package com.salvai.snake.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.salvai.snake.actors.Apple;
import com.salvai.snake.actors.Block;
import com.salvai.snake.actors.GameObjectMap;
import com.salvai.snake.actors.snake.Snake;
import com.salvai.snake.actors.snake.SnakeBody;
import com.salvai.snake.enums.MovingDirection;
import com.salvai.snake.screens.GameScreen;

public class GameFlowManager {
    public Snake snake;
    public Array<Block> baseBlocks;
    public Array<Block> blocks;
    public Apple apple;
    private GameScreen gameScreen;
    private Texture blockTexture;
    private BoundariesCreator boundariesCreator;
    private GameObjectMap gameObjectMap;


    public GameFlowManager(GameScreen screen) {
        this.gameScreen = screen;
        this.gameScreen.game.savePreferences();

        blockTexture = gameScreen.game.assetsManager.manager.get(Constants.BLOCK_IMAGE_NAME, Texture.class);

        boundariesCreator = new BoundariesCreator(blockTexture, gameScreen.game.worldUtils);

        baseBlocks = boundariesCreator.fullBoundaries();
        blocks = new Array<Block>();

        //load blocks
        blocks = this.gameScreen.game.getCurrentLevel().getBlocks(blockTexture, gameScreen.game.worldUtils, gameScreen.game.selectedColor);

        //Load blocks first other wise start X is aways -1. See getBocks method
        if (gameScreen.game.firstTimeOpen)
            snake = new Snake(blockTexture, 8, 19, gameScreen.game.selectedColor, gameScreen.game.worldUtils);
        else if (gameScreen.game.getCurrentLevel().snakeStartX > 0)
            snake = new Snake(blockTexture, gameScreen.game.getCurrentLevel().snakeStartX, gameScreen.game.getCurrentLevel().snakeStartY, gameScreen.game.selectedColor, gameScreen.game.worldUtils);
        else
            snake = new Snake(blockTexture, gameScreen.game.selectedColor, gameScreen.game.worldUtils);

        gameObjectMap = new GameObjectMap(blocks, gameScreen.game.worldUtils);


        if (gameScreen.game.firstTimeOpen)
            apple = new Apple(new Vector2(11, 19), blockTexture, gameScreen.game.worldUtils, gameScreen.game.selectedColor);
        else
            apple = new Apple(gameObjectMap.getFreePositions(snake, null), blockTexture, gameScreen.game.worldUtils, gameScreen.game.selectedColor);


    }

    public SnakeBody update(MovingDirection userDirection) {
        SnakeBody newSnakeBody = null;

        if (userDirection != null && userDirection != snake.snakeHead.direction) {
            if (gameScreen.game.soundOn)
                playMoveSound(userDirection);
            snake.setDirection(userDirection);
        }

        snake.moveWorldPosition();

        if (snake.checkGameOver(blocks)) {
            gameScreen.gameOver = true;
            if (gameScreen.game.vibrationOn) {
                Gdx.input.vibrate(Constants.VIBRATION_DURATION_GAME_OVER);
            }
            assignHighscore();
            playGameOverSound();
        } else {
            snake.move(gameScreen.game.worldUtils.blockSize);
            checkScore();
            newSnakeBody = addBody();
            snake.updateBodyAndTailDirections();
        }
        return newSnakeBody;
    }

    private void playGameOverSound() {
        if (gameScreen.game.soundOn)
            if (gameScreen.newHighscore)
                gameScreen.newBestSound.play();
            else
                gameScreen.gameOverSound.play();
    }

    private void playMoveSound(MovingDirection userDirection) {
        switch (userDirection) {
            case UP:
                gameScreen.upSound.play();
                break;
            case DOWN:
                gameScreen.downSound.play();
                break;
            default:
                gameScreen.leftRightSound.play();
                break;
        }
    }

    private SnakeBody addBody() {
        SnakeBody newSnakeBody = null;
        if (snake.addBody) {
            if (gameScreen.game.vibrationOn)
                Gdx.input.vibrate(Constants.VIBRATION_DURATION);
            newSnakeBody = snake.addBody();
        }
        return newSnakeBody;
    }

    private void checkScore() {
        if (snake.eats(apple)) {
            gameScreen.game.score += Constants.POINT;
            gameScreen.updateScoreLabel();
            if (gameScreen.game.soundOn)
                gameScreen.pointSound.play();

            apple.reset(gameObjectMap.getFreePositions(snake, apple));
        }
    }

    private void assignHighscore() {
        if (gameScreen.game.highScores[gameScreen.game.level] < gameScreen.game.score) {
            gameScreen.game.highScores[gameScreen.game.level] = gameScreen.game.score;
            gameScreen.newHighscore = true;
        }
        gameScreen.game.savePreferences();
    }
}
