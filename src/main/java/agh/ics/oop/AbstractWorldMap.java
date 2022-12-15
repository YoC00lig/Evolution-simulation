package agh.ics.oop;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class AbstractWorldMap implements IPositionChangeObserver{
    public Vector2d low, high;
    public int width, height, minReproductionEnergy, plantEnergy, epoch, initialEnergy, moveEnergy;
    protected LinkedHashMap<Vector2d, LinkedList<Animal>> animals = new LinkedHashMap<>();
    public ArrayList<Animal> listOfAnimals = new ArrayList<>();
    public LinkedHashMap<Vector2d, Grass> grasses = new LinkedHashMap<>();
    protected LinkedHashMap<Vector2d,InfoField> fields1;
    protected boolean predistinationMode, toxicDeadMode, isCrazyMode, hellExistsMode;
    //for statistics
    int deadAnimals = 0;
    int daysOfLifeDeadsSum = 0;
    int livingAnimals = 0;
    int plantsNumber = 0;
    int dominantGenotype = 0;
    int averageEnergy, averageLifeLength, freeFields;


    public AbstractWorldMap(int width, int height,boolean predistination, boolean toxicMode, boolean isCrazyMode, boolean hellExistsMode, int reproductionE, int plantE, int initialE) {
        this.width = width;
        this.height = height;
        this.low = new Vector2d(0,0);
        this.high = new Vector2d(this.width-1,this.height-1);
        this.epoch = 1;
        this.freeFields = this.width * this.height;
        this.predistinationMode = predistination;
        this.toxicDeadMode = toxicMode;
        this.isCrazyMode = isCrazyMode;
        this.hellExistsMode = hellExistsMode;
        this.fields1 = generateFields1();
        this.plantEnergy = plantE;
        this.minReproductionEnergy = reproductionE;
        this.initialEnergy = initialE;
    }

    public LinkedHashMap<Vector2d, InfoField> generateFields1() {
        LinkedHashMap<Vector2d, InfoField> Field = new LinkedHashMap<>();
        for (int i = low.x ; i <= high.x; i++){
            for (int j = low.y ; j <= high.y; j++) {
                InfoField info = new InfoField();
                Field.put(new Vector2d(i, j), info);
            }
        }
        return Field;
    }

    // for statistics
    public void averageEnergy() {
        int numberOfAnimals = 0;
        int allenergy = 0;
        for (Vector2d position : animals.keySet()) {
            for (Animal animal : animals.get(position)) {
                numberOfAnimals += 1 ;
                allenergy += animal.getCurrentEnergy();
            }
        }
        this.averageEnergy = allenergy / numberOfAnimals;
    }

    public void averageLifeLength() {
        if (deadAnimals >= 1) this.averageLifeLength = daysOfLifeDeadsSum / deadAnimals;
    }

    public void freeFields() {
        int result = 0;
        for (InfoField info: fields1.values()){
            if (info.elements == 0) result += 1;
        }
        this.freeFields = result;
    }

    public void findDominantGenotype() {
        int[] cnt = new int[8];
        for (Animal animal: listOfAnimals) {
            int gen = animal.findDominantGenotype();
            cnt[gen] += 1;
        }
        int maxi = 0;
        int Gen = 0;

        for (int i = 0; i < 8; i++) {
            if (cnt[i] > maxi) {
                Gen = i;
                maxi = cnt[i];
            }
        }
        this.dominantGenotype = Gen;
    }

    // positions checking etc
    public boolean canMoveTo(Vector2d position) {
        return position.follows(this.low) && position.precedes(this.high);
    }

    @Override
    public void positionChanged(Animal animal, Vector2d oldPosition, Vector2d newPosition) {
        this.removeAnimal(animal, oldPosition);
        this.addAnimal(animal, newPosition);
    }

    public Vector2d HellsPortal(){
        int x = (int) (Math.random() * (high.x - low.x));
        int y = (int) (Math.random() * (high.y - low.y));
        return new Vector2d(x, y);
    }

    public void place(Animal animal) {
        Vector2d pos = animal.getPosition();

        if (!(pos.x >= this.width || pos.x < 0 || pos.y >= this.height || pos.y < 0)){
            this.addAnimal(animal, pos);
            animal.addObserver(this);
        }
    }

    public AbstractWorldMapElement grassAt(Vector2d position) {
        return grasses.get(position);
    }

    // animal general functions
    public void moveAllAnimals() {
        for (LinkedList<Animal> listOfAnimals: this.animals.values()) {
            for (Animal animal : listOfAnimals) {
                animal.move2();
                animal.reduceEnergy();
            }
        }
    }

    public void addAnimal(Animal animal, Vector2d newPosition){
        fields1.get(newPosition).incrementElementsStatus();
        if (animals.get(newPosition) == null) {
            LinkedList<Animal> list = new LinkedList<>();
            list.add(animal);
            animals.put(newPosition, list);
            listOfAnimals.add(animal);
        }
        else animals.get(newPosition).add(animal);
    }

    public void removeAnimal(Animal animal, Vector2d oldPosition) {
        fields1.get(oldPosition).decrementElementsStatus();
        LinkedList<Animal> list =  animals.get(oldPosition);

        if (animals.get(oldPosition) != null) {
            list.remove(animal);
            listOfAnimals.remove(animal);
            animal.removeObserver(this);
            if (list.size() == 0) animals.remove(oldPosition);
        }
    }

    // przetestowac
    public void removeDead() {
        CopyOnWriteArrayList<Animal> bodiesToRemove = new CopyOnWriteArrayList<>();
        for (Vector2d position: animals.keySet()){
            for (Animal animal: animals.get(position)){
                if (animal != null) {
                   if (animal.isDead()) {
                       InfoField info = fields1.get(animal.getPosition());
                       info.incrementDeathStatus();
                       animal.isDead = animal.daysOfLife;
                       deadAnimals += 1;
                       livingAnimals -= 1;
                       daysOfLifeDeadsSum += animal.daysOfLife;
                       bodiesToRemove.add(animal);
                   }
                }
            }
        }

        for (Animal dead : bodiesToRemove) removeAnimal(dead, dead.getPosition());
    }

    // grass operations
    public void removeGrass(Grass grass) {
        grasses.remove(grass.getPosition(), grass);
    }

    public void plantGrass() { // funkcja zasadza jedną roślinkę
        Vector2d plantPosition;
        if (toxicDeadMode) { // wariant "toksyczne trupy"
//            fields.sort(new ComparatorForFieldDeath());
            for (Vector2d v : fields1.keySet()) {
                if (!(grassAt(v) instanceof Grass)) {
                    Grass element = new Grass(v, this);
                    plantPosition = v;
                    fields1.get(plantPosition).incrementElementsStatus();
                    grasses.put(v, element);
                    break;
                }
            }
        }

        else { // wariant "zalesione równiki" // todo
            int middle = this.width / 2; // todo
            boolean planted = false;

            for (int col = this.low.y; col <= this.high.y; col++) { // sadzimy na równiku
                Vector2d v = new Vector2d(middle, col);
                if (!(grassAt(v) instanceof Grass)) {
                    plantPosition = v;
                    fields1.get(plantPosition).incrementElementsStatus();
                    Grass element = new Grass(v, this);
                    grasses.put(v, element);
                    planted = true;
                    break;
                }
            }
            if (!planted){ // nie udało się zasadzić na równiku, wiec sadzimy gdzieś indziej
                for (int row = this.low.x; row <= this.high.x; row++){
                    for (int col = this.low.y; col <= this.high.y; col++){
                        if (row != middle){
                            Vector2d v = new Vector2d(row, col);
                            if (!(grassAt(v) instanceof Grass)) {
                                plantPosition = v;
                                fields1.get(plantPosition).incrementElementsStatus();
                                Grass element = new Grass(v, this);
                                grasses.put(v, element);
                                break;
                            }
                        }
                    }
                }
            }
        }

    }

    // reproduction
    public List<Animal> getParents(Vector2d position){
        List<Animal> parents = animals.get(position);
        parents.sort(new ComparatorForEnergy());
        return parents.subList(0, 2);
    }

    public Animal getBaby(List<Animal> parents){
        Animal parent1 = parents.get(0);
        Animal parent2 = parents.get(1);
        return new Animal(this, parent1, parent2);
    }

    // todo - getParents - new comparator
    public void reproduction() {
        for (Vector2d position : animals.keySet()){
            if (animals.get(position).size() >= 2) {

                List<Animal> parents = getParents(position);
                Animal stronger = Genotype.getStrongerWeaker(parents.get(0), parents.get(1))[0];
                Animal weaker = Genotype.getStrongerWeaker(parents.get(0), parents.get(1))[1];

                stronger.addNewChild();
                weaker.addNewChild();

                if (weaker.getCurrentEnergy() >= minReproductionEnergy) {
                    Animal baby = getBaby(parents);
                    stronger.reproduce(weaker);
                    InfoField info = fields1.get(baby.getPosition());
                    info.incrementElementsStatus();
                    this.place(baby);
                }
            }
        }
    }

    // eating
    public void rewrite(List<Animal> toUpdate) {
        for (Animal animal: toUpdate) {
            removeAnimal(animal, animal.getPosition());
            addAnimal(animal, animal.getPosition());
        }
    }
    // todo
    public void feed(List<Animal> Animals){
        List<Animal> toUpdate = new ArrayList<>();
        int gained = (int) Math.floor((float) plantEnergy / Animals.size());

        for (Animal animal:Animals){
            animal.setEnergy(animal.getCurrentEnergy() + gained);
            animal.atePlant();
            toUpdate.add(animal);
        }
        rewrite(toUpdate);
    }

    // todo - new comparator
    public List<Animal> findStrongestAtPos(Vector2d position) {
        this.animals.get(position).sort(new ComparatorForEnergy());
        List<Animal> list = this.animals.get(position);
        Animal strongest = list.get(0);

        int idx = 1;
        while (idx < list.size()){
            Animal current = list.get(idx);
            if (strongest.getCurrentEnergy() == current.getCurrentEnergy()) idx++; // todo
        }
        return list.subList(0, idx);
    }

    public void eat() {
        List<Grass> toUpdate = new ArrayList<>();
        for (Vector2d position : grasses.keySet()){
            if (this.animals.get(position) != null && this.animals.get(position).size() > 0) {
                List<Animal> strongestAnimals = findStrongestAtPos(position);
                feed(strongestAnimals);
                toUpdate.add(grasses.get(position));
            }
        }
        for (Grass element : toUpdate) {
            fields1.get(element.getPosition()).decrementElementsStatus();
            removeGrass(element);
            plantsNumber -= 1;
        }
    }

    // new day
    public void nextDay() {
        this.epoch+=1;
        for (LinkedList<Animal> listOfAnimals: this.animals.values()) {
            for (Animal animal : listOfAnimals) animal.aliveNextDay();
        }
    }

}
