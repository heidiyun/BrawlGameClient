package windows;

import models.Camera;
import models.UI;
import models.User;
import models.Vector2D;
import utils.*;
import com.google.gson.JsonObject;
import dwon.SpriteManager;
import processing.core.PApplet;
import states.*;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class Window2 extends PApplet implements Constants {
    private User user = new User(100, 100, "test", PLAYER_DOWN,
            100, 100, 100, 10, USER_STOP, true);
    private KeyEventManager keyEventManager = new KeyEventManager();
    private Communicator communicator;
    private Map myMap;
    private List<String> userNames;
    private ConcurrentHashMap<String, User> userLibrary;
    private UI ui;
    private int i = 0;
    private Camera camera;
    private int tick;
    private boolean isJoin = true;

    @Override
    public void settings() {
        size(WINDOW_SIZE_X, WINDOW_SIZE_Y);
    }


    @Override
    public void setup() {
        loadSound();
        SoundManager.loop(SOUND_THEME, 0);

        communicator = new Communicator("localhost", 5000);
        communicator.connect(user);
        userLibrary = new ConcurrentHashMap<>();
        userNames = new CopyOnWriteArrayList<>(userLibrary.keySet());

        user.setMe(true);
        camera = new Camera();

        communicator.setOnCommunicatorListener(new CommunicatorListener() {
            @Override
            public void onMapReceive(Map map) {
                myMap = map;
//                myMap.setUser(user);
            }

            @Override
            public void onHitReceive(Hit hit) {
                if (userLibrary.containsKey(hit.getTo())) {
                    userLibrary.get(hit.getTo()).setHit(true);
                }
            }

            @Override
            public void onUpdate(List<Update> updates) {
                for (Update u :
                        updates) {
                    if (!userLibrary.containsKey(u.getUser()))
                        userNames.add(u.getUser());

                    userLibrary.putIfAbsent(u.getUser(), new User(u.getX(), u.getY(),
                            u.getUser(), u.getDirection(), u.getHp(), u.getMana(),
                            u.getStamina(), u.getScore(), u.getState()));

                    if (u.getUser().equals(user.getName())) {
                        String userName = user.getName();
                        user = userLibrary.get(userName);
                        user.setMe(true);
                        user.setX(u.getX());
                        user.setY(u.getY());

                        user.onHpChange(u.getHp());
                        user.setHp(u.getHp());

                        user.onManaChange(u.getMana());
                        user.setMana(u.getMana());

                        user.setCharacterImage(u.getCharacterImage());
                        user.setStamina(u.getStamina());
                        user.setDirection(u.getDirection());
                        user.setScore(u.getScore());

                        user.onStateChange(u.getState());
                        user.setState(u.getState());
                        if (u.getState().equals("ATTACK") || u.getState().equals("SPECIAL")) {
                            user.setAttackDirection(u.getDirection());
                        }

                        user.setSpeed(u.getSpeed());
                        user.setPos(new Vector2D(myMap.getLenX(), myMap.getLenY()));
                    }

                    if (userLibrary.containsKey(u.getUser())) {
                        User user = userLibrary.get(u.getUser());
                        user.setX(u.getX());
                        user.setY(u.getY());

                        user.onHpChange(u.getHp());
                        user.setHp(u.getHp());

                        user.onManaChange(u.getMana());
                        user.setMana(u.getMana());

                        user.setCharacterImage(u.getCharacterImage());
                        user.setStamina(u.getStamina());
                        user.setDirection(u.getDirection());
                        user.setScore(u.getScore());
                        user.onStateChange(u.getState());
                        user.setState(u.getState());
                        user.setSpeed(u.getSpeed());
                        user.setPos(new Vector2D(myMap.getLenX(), myMap.getLenY()));
                    }
                }
            }

            @Override
            public void onKillReceive(Kill kill) {
                if (userLibrary.containsKey(kill.getTo())) {
                    ui.addKiller(kill.getFrom(), kill.getTo(), tick);
                }
            }

            @Override
            public void onMapCorrectReceive(int index, int message) {
                myMap.replaceIndex(index, message);
            }

            @Override
            public void onRejectReceive(JsonObject jsonObject) {
                isJoin = false;
                myMap.setLoad(false);
                System.out.println(jsonObject);
            }
        });

        loadImage();
        ui = new UI(userLibrary, userNames);

        addPressListeners();

        addReleaseListeners();
    }



    private void addPressListeners() {
        keyEventManager.addPressListener(LEFT, (isOnPress, duration) -> {
            if (isOnPress) {
                communicator.sendMove(new Move("LEFT"));
//                user.setAttackDirection("LEFT");
                user.setAttack(false);
                user.setSpecial(false);
                user.setDirection(PLAYER_LEFT);
                user.setState(USER_MOVE);
                myMap.setLenX(myMap.getLenX() + PLAYER_SPEED * 2);
            }
        });

        keyEventManager.addPressListener(RIGHT, (isOnPress, duration) -> {
            if (isOnPress) {
                communicator.sendMove(new Move("RIGHT"));
//                user.setAttackDirection("RIGHT");
                user.setAttack(false);
                user.setSpecial(false);
                user.setDirection(PLAYER_RIGHT);
                user.setState(USER_MOVE);
                myMap.setLenX(myMap.getLenX() - PLAYER_SPEED * 2);
            }
        });

        keyEventManager.addPressListener(UP, (isOnPress, duration) -> {
            if (isOnPress) {
                communicator.sendMove(new Move("UP"));
//                user.setAttackDirection("UP");
                user.setAttack(false);
                user.setSpecial(false);
                user.setDirection(PLAYER_UP);
                user.setState(USER_MOVE);
                myMap.setLenY(myMap.getLenY() + PLAYER_SPEED * 2);
            }
        });

        keyEventManager.addPressListener(DOWN, (isOnPress, duration) -> {
            if (isOnPress) {
                communicator.sendMove(new Move("DOWN"));
//                user.setAttackDirection("DOWN");
                user.setAttack(false);
                user.setSpecial(false);
                user.setDirection(PLAYER_DOWN);
                user.setState(USER_MOVE);
                myMap.setLenY(myMap.getLenY() - PLAYER_SPEED * 2);
            }
        });

        keyEventManager.addPressListener(67, (isOnPress, duration) -> {
            if (isOnPress && user.getMana() >= 40) {
                user.setAttackDirection(user.getDirection());
                user.setSpecial(true);
                communicator.sendSpecial();
            }
        });

        keyEventManager.addPressListener(32, (isOnPress, duration) -> {
            if (isOnPress) {
                user.setAttackDirection(user.getDirection());
                user.setAttack(true);
                communicator.sendAttack();
            }
        });

        keyEventManager.addPressListener(88, (isOnPress, duration) -> {
            if (isOnPress && user.getStamina() > 300) {
                user.setState(USER_SWIFT);
                communicator.sendSwift();
            } else if (user.getStamina() < 300){
                user.setTired(true);
            }
        });
    }

    private void addReleaseListeners() {
        keyEventManager.addReleaseListener(LEFT, duration -> {
            communicator.sendStop();
            communicator.sendCharacterImageNum(new Image(user.getCharacterImage() / 10 * 10 + 2));
            user.setState("STOP");
        });

        keyEventManager.addReleaseListener(RIGHT, duration -> {
            communicator.sendStop();
            communicator.sendCharacterImageNum(new Image(user.getCharacterImage() / 10 * 10 + 3));
            user.setState("STOP");
        });

        keyEventManager.addReleaseListener(UP, duration -> {
            communicator.sendStop();
            communicator.sendCharacterImageNum(new Image(user.getCharacterImage() / 10 * 10));
            user.setState("STOP");
        });

        keyEventManager.addReleaseListener(DOWN, duration -> {
            communicator.sendStop();
            communicator.sendCharacterImageNum(new Image(user.getCharacterImage() / 10 * 10 + 1));
            user.setState("STOP");
        });

        keyEventManager.addReleaseListener(32, duration -> {
            communicator.sendStop();
        });

        keyEventManager.addReleaseListener(67, duration -> {
            communicator.sendStop();
        });

        keyEventManager.addReleaseListener(88, duration -> {
            communicator.sendStop();
        });
    }

    private void showRejectMessage() {
        fill(0, 0, 255);
        textSize(30);
        text("your name is already used. \nplease change it to another name.", 100, 300);

    }

    @Override
    public void draw() {
        tick++;
        background(255);

        if (myMap.isLoad()) {

            camera.position.x = user.getX() - (WINDOW_SIZE_X - 200) / 2;
            camera.position.y = user.getY() - WINDOW_SIZE_Y / 2;

            myMap.onUpdate(camera);
            myMap.render(this);

            for (String user : userNames) {
                userLibrary.get(user).onUpdate(camera);
                userLibrary.get(user).render(this);
                if (userLibrary.get(user).getIsTime()) {
                    communicator.sendStop();
                    userLibrary.get(user).setIsTime(false);
                }
            }

            ui.render(this, tick);
            myMap.minimapRender(this);

            keyEventManager.update();

            for (String user : userNames) {
                userLibrary.get(user).miniRender(this);
            }

            if (mousePressed) {
                ui.checkUserName(mouseX, mouseY);
            }
        }

        if (isJoin && !myMap.isLoad()) {
            fill(255);
            fill(0, 255, 0);
            textSize(40);
            text("please choose your character >> ", 150, 100);
            textSize(20);
            fill(0);
            image(SpriteManager.getImage(CHARACTER_ONE_DOWN, 0), 200, 150, 100, 100);
            text("NUM 1", 220, 280);
            image(SpriteManager.getImage(CHARACTER_TWO_DOWN, 0), 400, 150, 100, 100);
            text("NUM 2", 420, 280);
            image(SpriteManager.getImage(CHARACTER_THREE_DOWN, 0), 600, 150, 100, 100);
            text("NUM 3", 620, 280);
            image(SpriteManager.getImage(CHARACTER_FOUR_DOWN, 0), 200, 300, 100, 100);
            text("NUM 4", 220, 430);
            image(SpriteManager.getImage(CHARACTER_FIVE_DOWN, 0), 400, 300, 100, 100);
            text("NUM 5", 420, 430);
            image(SpriteManager.getImage(CHARACTER_SIX_DOWN, 0), 600, 300, 100, 100);
            text("NUM 6", 620, 430);
            image(SpriteManager.getImage(CHARACTER_SEVEN_DOWN, 0), 200, 450, 100, 100);
            text("NUM 7", 220, 580);
        }

        if (!isJoin) {
            showRejectMessage();
        }
    }

    @Override
    public void mousePressed() {
        if (mouseX > ui.getMuteButton().getPos().x &&
                mouseX < ui.getMuteButton().getPos().x + BLOCK_SIZE &&
                mouseY > ui.getMuteButton().getPos().y &&
                mouseY < ui.getMuteButton().getPos().y + BLOCK_SIZE) {
            if (ui.getMuteButton().isClicked()) {
                SoundManager.stop(SOUND_THEME, 0);
                ui.getMuteButton().setClicked(false);
            } else {
                SoundManager.stop(SOUND_THEME, 0);
                ui.getMuteButton().setClicked(true);
            }
        }
    }

    public void keyPressed() {

        if (isJoin && !myMap.isLoad()) {
            switch (keyCode) {
                case '1':
                    user.setCharacterImage(Constants.CHARACTER_ONE_UP);
                    communicator.sendCharacterImageNum(new Image(10));
                    myMap.setLoad(true);
                    break;
                case '2':
                    user.setCharacterImage(Constants.CHARACTER_TWO_UP);
                    communicator.sendCharacterImageNum(new Image(20));
                    myMap.setLoad(true);
                    break;
                case '3':
                    user.setCharacterImage(Constants.CHARACTER_THREE_UP);
                    communicator.sendCharacterImageNum(new Image(30));
                    myMap.setLoad(true);
                    break;
                case '4':
                    user.setCharacterImage(Constants.CHARACTER_FOUR_UP);
                    communicator.sendCharacterImageNum(new Image(40));
                    myMap.setLoad(true);
                    break;
                case '5':
                    user.setCharacterImage(Constants.CHARACTER_FIVE_UP);
                    communicator.sendCharacterImageNum(new Image(50));
                    myMap.setLoad(true);
                    break;
                case '6':
                    user.setCharacterImage(Constants.CHARACTER_SIX_UP);
                    communicator.sendCharacterImageNum(new Image(60));
                    myMap.setLoad(true);
                    break;
                case '7':
                    user.setCharacterImage(Constants.CHARACTER_SEVEN_UP);
                    communicator.sendCharacterImageNum(new Image(70));
                    myMap.setLoad(true);
                    break;
            }
        }
        if (isJoin && myMap.isLoad()) {
            keyEventManager.setPress(keyCode);
        }
    }

    public void keyReleased() {

        if (myMap.isLoad()) {
            keyEventManager.setRelease(keyCode);
        }
    }

    public void loadImage() {
        SpriteManager.loadSprite(this, CHARACTER_ONE_DOWN, "./image/image.png", 32, 32, new int[]{0, 1, 2, 1});
        SpriteManager.loadSprite(this, CHARACTER_ONE_LEFT, "./image/image.png", 32, 32, new int[]{12, 13, 14, 13});
        SpriteManager.loadSprite(this, CHARACTER_ONE_RIGHT, "./image/image.png", 32, 32, new int[]{24, 25, 26, 25});
        SpriteManager.loadSprite(this, CHARACTER_ONE_UP, "./image/image.png", 32, 32, new int[]{36, 37, 38, 37});

        SpriteManager.loadSprite(this, CHARACTER_TWO_DOWN, "./image/image.png", 32, 32, new int[]{3, 4, 5, 3});
        SpriteManager.loadSprite(this, CHARACTER_TWO_LEFT, "./image/image.png", 32, 32, new int[]{15, 16, 17, 16});
        SpriteManager.loadSprite(this, CHARACTER_TWO_RIGHT, "./image/image.png", 32, 32, new int[]{27, 28, 29, 28});
        SpriteManager.loadSprite(this, CHARACTER_TWO_UP, "./image/image.png", 32, 32, new int[]{39, 40, 41, 40});

        SpriteManager.loadSprite(this, CHARACTER_THREE_DOWN, "./image/image.png", 32, 32, new int[]{6, 7, 8, 7});
        SpriteManager.loadSprite(this, CHARACTER_THREE_LEFT, "./image/image.png", 32, 32, new int[]{18, 19, 20, 19});
        SpriteManager.loadSprite(this, CHARACTER_THREE_RIGHT, "./image/image.png", 32, 32, new int[]{30, 31, 32, 31});
        SpriteManager.loadSprite(this, CHARACTER_THREE_UP, "./image/image.png", 32, 32, new int[]{42, 43, 44, 43});

        SpriteManager.loadSprite(this, CHARACTER_FOUR_DOWN, "./image/image.png", 32, 32, new int[]{9, 10, 11, 10});
        SpriteManager.loadSprite(this, CHARACTER_FOUR_LEFT, "./image/image.png", 32, 32, new int[]{21, 22, 23, 22});
        SpriteManager.loadSprite(this, CHARACTER_FOUR_RIGHT, "./image/image.png", 32, 32, new int[]{33, 34, 35, 34});
        SpriteManager.loadSprite(this, CHARACTER_FOUR_UP, "./image/image.png", 32, 32, new int[]{45, 46, 47, 46});

        SpriteManager.loadSprite(this, CHARACTER_FIVE_DOWN, "./image/image.png", 32, 32, new int[]{48, 49, 50, 49});
        SpriteManager.loadSprite(this, CHARACTER_FIVE_LEFT, "./image/image.png", 32, 32, new int[]{60, 61, 62, 61});
        SpriteManager.loadSprite(this, CHARACTER_FIVE_RIGHT, "./image/image.png", 32, 32, new int[]{72, 73, 74, 73});
        SpriteManager.loadSprite(this, CHARACTER_FIVE_UP, "./image/image.png", 32, 32, new int[]{84, 85, 86, 85});

        SpriteManager.loadSprite(this, CHARACTER_SIX_DOWN, "./image/image.png", 32, 32, new int[]{51, 52, 53, 52});
        SpriteManager.loadSprite(this, CHARACTER_SIX_LEFT, "./image/image.png", 32, 32, new int[]{63, 64, 65, 64});
        SpriteManager.loadSprite(this, CHARACTER_SIX_RIGHT, "./image/image.png", 32, 32, new int[]{75, 76, 77, 76});
        SpriteManager.loadSprite(this, CHARACTER_SIX_UP, "./image/image.png", 32, 32, new int[]{87, 88, 89, 88});

        SpriteManager.loadSprite(this, CHARACTER_SEVEN_DOWN, "./image/image.png", 32, 32, new int[]{54, 55, 56, 55});
        SpriteManager.loadSprite(this, CHARACTER_SEVEN_LEFT, "./image/image.png", 32, 32, new int[]{66, 67, 68, 67});
        SpriteManager.loadSprite(this, CHARACTER_SEVEN_RIGHT, "./image/image.png", 32, 32, new int[]{78, 79, 80, 79});
        SpriteManager.loadSprite(this, CHARACTER_SEVEN_UP, "./image/image.png", 32, 32, new int[]{90, 91, 92, 91});


        SpriteManager.loadSprite(this, FIST, "./image/super_dragon_fist_effect.png", 0, 0,
                192, 192, 6);
        SpriteManager.loadImage(this, UI, "./image/ui/ui.png");
        SpriteManager.loadImage(this, ARROWUP, "./image/ui/arrowup.png");
        SpriteManager.loadImage(this, ARROWDOWN, "./image/ui/arrowdown.png");

        SpriteManager.loadImage(this, GRASS, "./image/tile/grass.png");
        SpriteManager.loadImage(this, SLOW_TILE, "./image/tile/tiles.png", 1, 1, 32, 32);
        SpriteManager.loadSprite(this, HEAL_POTION, "./image/tile/potion.png", 0, 0, 50, 63, 7);

        SpriteManager.loadImage(this, FIRE_ATTACK_UP_1, "./image/fireblast/up1.png");
        SpriteManager.loadImage(this, FIRE_ATTACK_UP_2, "./image/fireblast/up2.png");
        SpriteManager.loadImage(this, FIRE_ATTACK_DOWN_1, "./image/fireblast/down1.png");
        SpriteManager.loadImage(this, FIRE_ATTACK_DOWN_2, "./image/fireblast/down2.png");
        SpriteManager.loadImage(this, FIRE_ATTACK_LEFT_1, "./image/fireblast/left1.png");
        SpriteManager.loadImage(this, FIRE_ATTACK_LEFT_2, "./image/fireblast/left2.png");
        SpriteManager.loadImage(this, FIRE_ATTACK_RIGHT_1, "./image/fireblast/right1.png");
        SpriteManager.loadImage(this, FIRE_ATTACK_RIGHT_2, "./image/fireblast/right2.png");

        SpriteManager.loadSprite(this, MANA_POTION, "./image/tile/potions.png", 0, 2, 430, 500, 1);

        SpriteManager.loadSprite(this, PUNCH_UP, "./image/punch/punch_up.png", 0, 0, 50, 80, 4);
        SpriteManager.loadSprite(this, PUNCH_DOWN, "./image/punch/punch_down.png", 0, 0, 50, 80, 4);
        SpriteManager.loadSprite(this, PUNCH_LEFT, "./image/punch/punch_left.png", 0, 0, 80, 50, 4);
        SpriteManager.loadSprite(this, PUNCH_RIGHT, "./image/punch/punch_right.png", 0, 0, 80, 50, 4);

        SpriteManager.loadImage(this, BUTTON_MUTE, "./image/ui/mute.png");
        SpriteManager.loadImage(this, BUTTON_UNMUTE, "./image/ui/unmute.png");
    }

    public void loadSound() {
        SoundManager.loadSound(SOUND_THEME, 0,"./sound/theme.wav");
        SoundManager.loadSound(SOUND_HIT,0,"./sound/hit/hit05.wav");
        SoundManager.loadSound(SOUND_HIT,1,"./sound/hit/hit06.wav");
        SoundManager.loadSound(SOUND_HIT,2,"./sound/hit/hit07.wav");
        SoundManager.loadSound(SOUND_FIRE, 0,"./sound/fire.wav");
        SoundManager.loadSound(SOUND_HP,0, "./sound/hp.wav");
        SoundManager.loadSound(SOUND_MANA, 0,"./sound/mana.wav");
        SoundManager.loadSound(SOUND_PUNCH, 0,"./sound/punch.wav");

    }
}