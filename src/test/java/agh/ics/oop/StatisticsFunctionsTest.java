package agh.ics.oop;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StatisticsFunctionsTest {

    // przeciętna długośc życia
    @Test
    public void Test1() { // test przypadku sredniej dlugosci zycia kiedy żadne jeszcze nie umarło
        AbstractWorldMap map = new AbstractWorldMap(5, 5, true,true,true,true, 3, 4, 5, 1);
        Animal animal1 = new Animal(map, new Vector2d(2,3));
        Animal animal2 = new Animal(map, new Vector2d(3,1));

        map.averageLifeLength();
        assertEquals(map.averageLifeLength, 0);
    }

    @Test
    public void Test2() { // jedno żyło 4 dni, drugie 1 = floor( (4+1)/2) ) = 2
        AbstractWorldMap map = new AbstractWorldMap(5, 5, true,true,true,true, 3, 4, 5, 1);
        Animal animal1 = new Animal(map, new Vector2d(2,3));
        Animal animal2 = new Animal(map, new Vector2d(3,1));

        animal1.aliveNextDay();
        animal1.aliveNextDay();
        animal1.aliveNextDay();

        animal1.setEnergy(0);
        animal2.setEnergy(0);

        map.averageLifeLength();
        assertEquals(map.averageLifeLength, 2);

    }

    @Test
    public void Test3() {
        AbstractWorldMap map = new AbstractWorldMap(5, 5, true,true,true,true, 3, 4, 5, 1);

        for (Integer dist: map.fields2.keySet()) System.out.println(dist);

    }
}
