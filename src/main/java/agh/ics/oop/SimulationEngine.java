package agh.ics.oop;

import agh.ics.oop.gui.App;
import agh.ics.oop.gui.EvolutionWindow;
import javafx.application.Platform;

import java.io.FileNotFoundException;
import java.util.Random;


public class SimulationEngine implements IEngine, Runnable{
    private final AbstractWorldMap map;
    private final int startAnimalsNumber;
    private final int startGrassnumber;
    private final int dailyGrowersNumber;
    private boolean isActive;
    private final int moveDelay = 10;
    public Statistics stats;
    private final EvolutionWindow window;

    public SimulationEngine(AbstractWorldMap map, int animalsNumber, int grassNumber, int dailyGrassNumber, EvolutionWindow window){
        this.map = map;
        this.stats = new Statistics(map);
        this.startAnimalsNumber = animalsNumber;
        this.startGrassnumber = grassNumber;
        this.dailyGrowersNumber = dailyGrassNumber;
        this.window = window;
        this.isActive = true;

        for (int i = 0; i < startAnimalsNumber; i++){
            Random random = new Random();
            int x = random.nextInt(map.high.x + 1 - map.low.x) + map.low.x;
            int y = random.nextInt(map.high.y + 1 - map.low.y) + map.low.y;
            Vector2d position = new Vector2d(x,y);
            new Animal(this.map, position);
        }
        for (int i = 0; i < grassNumber; i++) {
            map.plantGrass();
        }
    }


    @Override
    public void run() {
        while (map.listOfAnimals.size()>0) {
            if (this.isActive) {
                if (map.listOfAnimals.size() == 0) {
                    System.out.println("(SimulationEngine-run) Wszystkie zwierzątka zmarły. Ilość dni: " + map.day);
                    throw new RuntimeException();
//                    System.exit(0);
                }

                map.removeDead();
                map.moveAll();
                map.eat();
                map.reproduction();
                for (int i = 0; i < dailyGrowersNumber; i++) map.plantGrass();
                map.freeFields();
                map.nextDay();


            }
//            try {
//                System.out.println("dziala");
//                application.draw();
//            } catch (FileNotFoundException e) {
//                throw new RuntimeException(e);
//            }

            try {
                Thread.sleep(this.moveDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> {
                try {
                    System.out.println("dziala2");
                    window.drawGame();
                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
        }
        if (map.listOfAnimals.size() == 0) {
            System.out.println("(SimulationEngine-run) Wszystkie zwierzątka zmarły. Ilość dni: " + map.day);
            throw new RuntimeException();
//                System.exit(0);
        }
    }

    public void activate() {this.isActive = true;}
    public void deactivate() {this.isActive = false;}
    public boolean getStatus() {return this.isActive;}

}

