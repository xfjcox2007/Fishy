import tester.*;
import javalib.worldimages.*;
import javalib.funworld.*;
import java.awt.Color;
import java.util.Random;

// PLAYERFISH
// to represent a fish
class Fish {
  Posn center;
  int radius;
  Color color;

  // the constructor
  Fish(Posn center, int radius, Color color) {
    this.center = center;
    this.radius = radius;
    this.color = color;
  }

  // produces the image of a fish
  WorldImage fishImage() {
    return new CircleImage(this.radius, "solid", this.color);
  }

  // ON KEY EVENT
  // move the PlayerFish 5 pixels in the direction given by the key
  Fish moveFish(String ke) {
    if (ke.equals("right")) {
      return new Fish(new Posn(this.center.x + 5, this.center.y), this.radius, this.color);
    }
    else if (ke.equals("left")) {
      return new Fish(new Posn(this.center.x - 5, this.center.y), this.radius, this.color);
    }
    else if (ke.equals("up")) {
      return new Fish(new Posn(this.center.x, this.center.y - 5), this.radius, this.color);
    }
    else if (ke.equals("down")) {
      return new Fish(new Posn(this.center.x, this.center.y + 5), this.radius, this.color);
    }
    else {
      return this;
    }
  }

  // ON TICK
  // the player fish can loop around
  Fish reEnter() {
    if (this.center.x >= 400 + this.radius) {
      return new Fish(new Posn(-this.radius, this.center.y), this.radius, this.color);
    }
    if (this.center.x <= -this.radius) {
      return new Fish(new Posn(400 + this.radius, this.center.y), this.radius, this.color);
    }
    return this;
  }

  // to generate a new fish
  Fish addMoreFish(int height) {
    return new Fish(new Posn(0, this.randomInt(height)), this.randomRadius(),
        new Color(new Random().nextInt(255), new Random().nextInt(255), new Random().nextInt(255)));
  }

  // helper method to generate a random number in the range -n to n
  int randomInt(int height) {
    return (new Random().nextInt(height));
  }

  // helper method to generate a random number for the radius
  int randomRadius() {
    return 1 + (new Random().nextInt(90));
  }

  // the background fishes move 5 pixels
  Fish moveBgFish() {
    return new Fish(new Posn(this.center.x + 5, this.center.y), this.radius, this.color);

  }

  // determines if two fish are in close proximity
  boolean isClose(Fish player) {
    return ((this.center.x - player.center.x) * (this.center.x - player.center.x)
        + (this.center.y - player.center.y) * (this.center.y - player.center.y)) < (this.radius
            + player.radius) * (this.radius + player.radius);
  }

  // to determine if the player can eat the fish
  boolean canEat(Fish player) {
    return this.isClose(player) && this.radius <= player.radius;
  }

  // END THE GAME
  // determines if a player is eaten by a larger fish, ending the game if true
  boolean isBiggerHelp(Fish that) {
    return this.isClose(that) && this.radius >= that.radius;
  }

}

// BACKGROUND FISH
// to represent a list of fish
interface ILoFish {
  WorldScene bgFishImage(WorldScene background);

  // ON TICK
  // to move the background fishes by 5 pixels
  ILoFish moveILoFish();

  // to add a new fish in the list if a fish is eaten by the player
  ILoFish addMoreFishes(int width, int height);

  // to delete the eaten fish from the list
  ILoFish canBeEaten(Fish player);

  // to produce a larger player fish
  Fish growUp(Fish player);

  // END THE GAME
  // to determine if the player fish is the biggest one on the canvas
  // If so, then stop the game
  boolean isBiggest(Fish player);

  // determines if a player is eaten by a larger fish, ending the game if true
  boolean isBigger(Fish player);
}

// a class representing an empty list
class MtLoFish implements ILoFish {

  // to produce the world scene
  public WorldScene bgFishImage(WorldScene background) {
    return background;
  }

  // ON TICK
  // to move the background fishes by 5 pixels
  public ILoFish moveILoFish() {
    return this;
  }

  // to add a new fish in the list if a fish is eaten by the player
  public ILoFish addMoreFishes(int width, int height) {
    return this;
  }

  // to delete the eaten fish from the list
  public ILoFish canBeEaten(Fish player) {
    return this;
  }

  // to produce a larger player fish
  public Fish growUp(Fish player) {
    return player;
  }

  // END THE GAME
  // to determine if the player fish is the biggest one in the canvas
  // If so, then stop the game
  public boolean isBiggest(Fish player) {
    return true;
  }

  // determines if a player is eaten by a larger fish, ending the game if true
  public boolean isBigger(Fish player) {
    return false;
  }

}

// a class representing a non-empty list of fish
class ConsLoFish implements ILoFish {
  Fish first;
  ILoFish rest;

  // constructor
  ConsLoFish(Fish first, ILoFish rest) {
    this.first = first;
    this.rest = rest;
  }

  public WorldScene bgFishImage(WorldScene background) {
    return this.rest.bgFishImage(
        background.placeImageXY(this.first.fishImage(), this.first.center.x, this.first.center.y));
  }

  // ON TICK
  // to move the background fishes by 5 pixels
  public ILoFish moveILoFish() {
    return new ConsLoFish(this.first.moveBgFish(), this.rest.moveILoFish());
  }

  // to add a new fish in the list if a fish is eaten by the player
  public ILoFish addMoreFishes(int width, int height) {
    if (this.first.center.x == width) {
      return new ConsLoFish(this.first.addMoreFish(height), this.rest.addMoreFishes(width, height));
    }
    else {
      return new ConsLoFish(this.first, this.rest.addMoreFishes(width, height));
    }
  }

  // to delete the eaten fish from the list
  public ILoFish canBeEaten(Fish player) {
    if (this.first.canEat(player)) {
      return new ConsLoFish(this.first.addMoreFish(300), rest);
    }
    else {
      return new ConsLoFish(this.first, this.rest.canBeEaten(player));
    }
  }

  // to produce a larger player fish
  public Fish growUp(Fish player) {
    if (this.first.canEat(player)) {
      return new Fish(player.center, player.radius + 10, player.color);
    }
    else {
      return this.rest.growUp(player);
    }
  }

  // END THE GAME
  // to determine if the player fish is the biggest one in the canvas
  // If so, then stop the game
  public boolean isBiggest(Fish player) {
    return this.first.radius < player.radius && this.rest.isBiggest(player);
  }

  // to determine if the player eats a bigger fish. If so, the stop the game
  public boolean isBigger(Fish player) {
    return this.first.isBiggerHelp(player) || this.rest.isBigger(player);
  }
}

// THE WORLD
// represent the world of the fishy
class FishyWorld extends World {
  int width = 400;
  int height = 300;
  Fish fish;
  ILoFish bg;

  // constructor
  FishyWorld(Fish fish, ILoFish bg) {
    super();
    this.fish = fish;
    this.bg = bg;
  }

  // to produce the image of the world by adding the player on the background
  // fish image
  public WorldScene makeScene() {
    return this.bg
        .bgFishImage(this.getEmptyScene()
            .placeImageXY(new RectangleImage(400, 300, "solid", Color.CYAN), 200, 150))
        .placeImageXY(this.fish.reEnter().fishImage(), this.fish.reEnter().center.x,
            this.fish.reEnter().center.y);

  }

  // On Key Event
  // move the player fish when the player presses a key
  public World onKeyEvent(String ke) {
    return new FishyWorld(this.fish.moveFish(ke), this.bg);
  }

  // On Tick
  // on tick, move and grow the player fish and move the background fish
  public World onTick() {
    return new FishyWorld(this.bg.growUp(this.fish).reEnter(),
        this.bg.moveILoFish().canBeEaten(this.fish).addMoreFishes(this.width, this.height));
  }

  // End the Game
  // There are two situations in which the game is stopped:
  // -- the player fish eats a larger fish
  // -- the player fish is the biggest fish in the canvas
  public WorldEnd worldEnds() {
    if (this.bg.isBigger(this.fish)) {
      return new WorldEnd(true, this.lastScene());
    }
    if (this.bg.isBiggest(this.fish)) {
      return new WorldEnd(true, this.lastScene());
    }
    else {
      return new WorldEnd(false, this.makeScene());
    }
  }

  // to produce the last scene
  public WorldScene lastScene() {
    return this.makeScene().placeImageXY(new TextImage("Game Over", Color.red), 300, 40);
  }
}

class ExamplesFishy {
  // the player fish
  Fish playerfish = new Fish(new Posn(100, 100), 15, Color.black);
  Fish fishleft = new Fish(new Posn(95, 100), 15, Color.black);
  Fish fishright = new Fish(new Posn(105, 100), 15, Color.black);
  Fish fishup = new Fish(new Posn(100, 95), 15, Color.black);
  Fish fishdown = new Fish(new Posn(100, 105), 15, Color.black);

  // the background fish
  Fish fish1 = new Fish(new Posn(10, 10), 10, Color.BLUE);
  Fish fish2 = new Fish(new Posn(30, 75), 5, Color.cyan);
  Fish fish3 = new Fish(new Posn(200, 30), 30, Color.DARK_GRAY);
  Fish fish4 = new Fish(new Posn(200, 150), 35, Color.gray);
  Fish fish5 = new Fish(new Posn(350, 250), 40, Color.orange);
  Fish fish6 = new Fish(new Posn(260, 150), 25, Color.PINK);
  Fish fish7 = new Fish(new Posn(400, 300), 25, Color.PINK);
  Fish fish8 = new Fish(new Posn(405, 300), 25, Color.PINK);

  ILoFish fishes1 = new MtLoFish();
  ILoFish fishes2 = new ConsLoFish(this.fish1, this.fishes1);
  ILoFish fishes3 = new ConsLoFish(this.fish2, this.fishes2);
  ILoFish fishes4 = new ConsLoFish(this.fish3, this.fishes3);
  ILoFish fishes5 = new ConsLoFish(this.fish4, this.fishes4);
  ILoFish fishes6 = new ConsLoFish(this.fish5, this.fishes5);
  ILoFish fishes7 = new ConsLoFish(this.fish6, this.fishes6);
  ILoFish fishes8 = new ConsLoFish(this.fishleft, this.fishes1);
  ILoFish fishes9 = new ConsLoFish(this.fish7, this.fishes1);
  ILoFish fishes10 = new ConsLoFish(this.fish8, this.fishes1);

  FishyWorld w1 = new FishyWorld(this.playerfish, this.fishes7);
  FishyWorld w2 = new FishyWorld(this.playerfish, this.fishes3);
  FishyWorld w3 = new FishyWorld(this.playerfish, this.fishes9);
  FishyWorld w4 = new FishyWorld(this.playerfish, this.fishes10);

  // testing moveFish method
  boolean testMoveFish(Tester t) {
    return t.checkExpect(this.playerfish.moveFish("left"), this.fishleft,
        "test moveBolb - left " + "\n")
        && t.checkExpect(this.playerfish.moveFish("right"), this.fishright,
            "test movelob - right " + "\n")
        && t.checkExpect(this.playerfish.moveFish("up"), this.fishup, "test moveBlob - up " + "\n")
        && t.checkExpect(this.playerfish.moveFish("down"), this.fishdown,
            "test moveBlob - down " + "\n")
        && t.checkExpect(this.playerfish.moveFish("/k"), this.playerfish);
  }

  // testing reEnter method
  boolean testReEnter(Tester t) {
    return t.checkExpect(this.playerfish.reEnter(), this.playerfish)
        && t.checkExpect(new Fish(new Posn(500, 40), 30, Color.BLACK).reEnter(),
            new Fish(new Posn(-30, 40), 30, Color.BLACK));
  }

  // testing addMoreFish method
  boolean testAddMoreFish(Tester t) {
    return t.checkNumRange(this.playerfish.center.y, 0, 300, true, true)
        && t.checkNumRange(this.playerfish.radius, 0, 90, true, true)
        && t.checkNumRange(this.playerfish.color.getBlue(), 0, 255, true, true)
        && t.checkNumRange(this.playerfish.color.getRed(), 0, 255, true, true)
        && t.checkNumRange(this.playerfish.color.getGreen(), 0, 255, true, true);
  }

  // testing the moveBgFish method
  boolean testMoveBgFish(Tester t) {
    return t.checkExpect(this.fishes1.moveILoFish(), this.fishes1)
        && t.checkExpect(this.playerfish.moveFish("left"), this.fishleft)
        && t.checkExpect(this.playerfish.moveFish("right"), this.fishright);
  }

  // testing isClose method
  boolean testIsClose(Tester t) {
    return t.checkExpect(this.playerfish.isClose(fish2), false)
        && t.checkExpect(this.playerfish.isClose(this.fishleft), true);
  }

  // testing canEat method
  boolean testCanEat(Tester t) {
    return t.checkExpect(this.playerfish.canEat(this.fish1), false)
        && t.checkExpect(this.playerfish.canEat(this.fishleft), true)
        && t.checkExpect(this.fishleft.canEat(this.playerfish), true);
  }

  // testing isBiggerHelper method
  boolean testIsBiggerHelper(Tester t) {
    return t.checkExpect(this.playerfish.isBiggerHelp(this.fish1), false)
        && t.checkExpect(this.playerfish.isBiggerHelp(this.fishright), true) && t.checkExpect(
            this.playerfish.isBiggerHelp(new Fish(new Posn(95, 100), 5, Color.BLUE)), true);
  }

  // testing moveILoFish method
  boolean testMoveILoFish(Tester t) {
    return t.checkExpect(this.fishes1.moveILoFish(), this.fishes1)
        && t.checkExpect(this.fishes2.moveILoFish(),
            new ConsLoFish(new Fish(new Posn(15, 10), 10, Color.BLUE), this.fishes1));
  }

  // testing addMoreFishes method
  boolean testAddMoreFishes(Tester t) {
    return t.checkExpect(this.fishes1.addMoreFishes(400, 300), this.fishes1)
        && t.checkExpect(this.fishes3.addMoreFishes(400, 300),
            new ConsLoFish(new Fish(new Posn(30, 75), 5, Color.cyan),
                new ConsLoFish(new Fish(new Posn(10, 10), 10, Color.blue), new MtLoFish())));
  }

  // testing canBeEatern method
  boolean testCanBeEaten(Tester t) {
    return t.checkExpect(this.fishes1.canBeEaten(playerfish), this.fishes1)
        && t.checkExpect(this.fishes1.canBeEaten(fish2), this.fishes1);

  }

  // testing growUp method
  boolean testGrowUp(Tester t) {
    return t.checkExpect(this.fishes1.growUp(playerfish), this.playerfish)
        && t.checkExpect(this.fishes2.growUp(playerfish), this.playerfish) && t.checkExpect(
            this.fishes7.growUp(fish4), new Fish(fish4.center, fish4.radius + 10, fish4.color));
  }

  // testing isBiggest method
  boolean testIsBiggest(Tester t) {
    return t.checkExpect(this.fishes1.isBiggest(playerfish), true)
        && t.checkExpect(this.fishes2.isBiggest(fish2), false)
        && t.checkExpect(this.fishes2.isBiggest(playerfish), true);
  }

  // testing isBigger method
  boolean testIsBigger(Tester t) {
    return t.checkExpect(this.fishes1.isBigger(playerfish), false)
        && t.checkExpect(this.fishes1.isBigger(fish2), false)
        && t.checkExpect(this.fishes3.isBigger(fish2), true);
  }

  // testing the onKeyEvent method
  boolean testOnKeyEvent(Tester t) {
    return t.checkExpect(this.w1.onKeyEvent("left"),
        new FishyWorld(w1.fish.moveFish("left"), w1.bg))
        && t.checkExpect(this.w1.onKeyEvent("k"), this.w1);
  }

  // test the method worldEnds
  boolean testWorldEnds(Tester t) {
    return t.checkExpect(w1.worldEnds(), new WorldEnd(false, w1.makeScene()))
        && t.checkExpect(w2.worldEnds(), new WorldEnd(true, w2.lastScene()));
  }

  // test the method lastScene
  boolean testLastScene(Tester t) {
    return t.checkExpect(w1.lastScene(),
        w1.makeScene().placeImageXY(new TextImage("Game Over", Color.red), 300, 40));
  }

  // test the method onTick()
  boolean testOnTick(Tester t) {
    return t.checkExpect(w3.onTick(), w4);
  }

  // test the method randomInt(int)
  boolean testRandomInt(Tester t) {
    return t.checkNumRange(fish4.randomInt(10), 0, 10, true, true);
  }

  // test the method randomInt(int)
  boolean testRandomRadius(Tester t) {
    return t.checkNumRange(fish4.randomRadius(), 1, 91, true, true);
  }

  // UNCOMMENT TO RUN THE PROGRAM!!
  // boolean runAnimation = this.w1.bigBang(400, 300, 0.3);
}
