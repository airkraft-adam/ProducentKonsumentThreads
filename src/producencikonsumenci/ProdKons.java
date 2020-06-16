package producencikonsumenci;


import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

class Producent implements Runnable {
    private Magazyn magazyn;
    public final String nazwa;
    Random random = new Random();

    public Producent(String naz, Magazyn mag) {
        nazwa = naz;
        magazyn = mag;
    }

    public void run() {
        while (ProdKons.shouldRun == true) {
            try {
                int liczba = random.nextInt(100);
                int czas = random.nextInt(1000);
                magazyn.wstaw(liczba);
                sleep(czas);
                System.out.println(nazwa + " wstawił " + liczba + " zasnoł na " + czas + " mls");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}

class Konsument implements Runnable {
    private Magazyn magazyn;
    public final String nazwa;
    private Random random = new Random();

    public Konsument(String naz, Magazyn mag) {
        nazwa = naz;
        magazyn = mag;
    }

    public void run() {
        while (ProdKons.shouldRun == true) {
            try {
                int kupił = magazyn.usuń();
                int czas = random.nextInt(1000);
                sleep(czas);
                System.out.println(nazwa + " kupił " + kupił + " zasnoł na " + czas + " mls");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }
}

class Magazyn {
    private int[] tab;
    private int pocz, ile;

    public Magazyn(int n) {
        if (n < 1 || n > 10) throw new IllegalArgumentException("niewłaściwa pojemność magazynu");
        tab = new int[n];
    }

    public synchronized boolean pusty() {
        return ile == 0;
    }

    public synchronized boolean pełny() {

        return ile == tab.length;
    }

    public synchronized void wstaw(int x) throws InterruptedException {
        while (pełny()) {
            System.out.println("magazyn pełny ");
            wait();
        }
        tab[(pocz + ile) % tab.length] = x;
        ile++;

        notifyAll();
    }

    public synchronized int usuń() throws InterruptedException {
        while (pusty()) {
            System.out.println("magazyn pusty");
            wait();
        }
        int x = tab[pocz];
        pocz = (pocz + 1) % tab.length;
        ile--;

        notifyAll();
        return x;
    }
}

public class ProdKons {
    static Boolean shouldRun = true;

    public static void main(String[] args) throws InterruptedException {

        Magazyn magazyn = new Magazyn(4);
        Thread p1 = new Thread(new Producent("prod-1", magazyn));
        Thread p2 = new Thread(new Producent("prod-2", magazyn));
        Thread k1 = new Thread(new Konsument("konsu-1", magazyn));
        Thread k2 = new Thread(new Konsument("konsu-2", magazyn));

        Runnable stop = () -> {
            ProdKons.shouldRun = false;
        };
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(stop, 10, TimeUnit.SECONDS);
        executor.shutdown();
        p1.start();
        p2.start();
        k1.start();
        k2.start();

    }

}
