import java.util.NoSuchElementException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;
import java.util.concurrent.CyclicBarrier;
import java.util.LinkedList;

public class Main {

    public static class Restaurante{
        private final Semaphore vagas = new Semaphore(5);
        private Lock trava_num = new ReentrantLock();
        private Lock trava_fila = new ReentrantLock();
        public int espaco;
        public boolean cheio;
        public CyclicBarrier mesa;
        public LinkedList<Cliente> fila;
        public int servidos;

        public Restaurante(LinkedList<Cliente> fila){
            this.cheio = false;
            this.espaco = 0;
            //this.mesa = mesa;
            this.fila = fila;
            this.servidos = 0;
        }

    }

    public static class Cliente implements Runnable{
        public int id;
        public Restaurante rest;
        public boolean chegou;

        public Cliente(int id, Restaurante rest){
            this.id = id;
            this.rest = rest;
            this.chegou = false;
        }

        public void comer() throws InterruptedException {
            rest.trava_num.lock();
            rest.vagas.acquire();
            rest.espaco += 1;
            if (rest.espaco == 5) {
                rest.cheio = true;
            }
            rest.trava_num.unlock();
            System.out.println("Cliente " + this.id + " está comendo");
            //Thread.sleep(1000);
            Thread.sleep(new Random().nextInt(1000));
            System.out.println("Cliente " + this.id + " está saindo");
            rest.trava_num.lock();
            rest.espaco -= 1;
            if (rest.cheio) {
                if (rest.espaco == 0) {
                    rest.cheio = false;
                    rest.vagas.release(5);
                    rest.trava_num.unlock();
                    this.chamada();
                }
                else {
                    rest.trava_num.unlock();
                }
            } else {
                rest.trava_num.unlock();
                rest.vagas.release();
            }
        }

        public void chamada() throws InterruptedException {
            while (!rest.cheio){
                if (!rest.fila.isEmpty()) {
                    rest.trava_fila.lock();
                    Cliente enviar = rest.fila.removeFirst();
                    Thread env = new Thread(enviar);
                    rest.trava_fila.unlock();
                    env.start();
                }
            }
        }

        @Override
        public void run() {
            try {
                if (!this.chegou) {
                    Thread.sleep(new Random().nextInt(10000)); // simulando tempo pra chegar, variabilidade alta ajuda a demonstrar o algoritmo
                    this.chegou = true;
                }
                System.out.println("Cliente " + this.id + " está tentando se sentar");
                if (!rest.cheio && rest.fila.isEmpty()) {
                    this.comer();
                }
                else {
                    rest.trava_fila.lock();
                    System.out.println("Cliente " + this.id + " vai pra fila");
                    rest.fila.addLast(this);
                    rest.trava_fila.unlock();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        LinkedList<Cliente> fila = new LinkedList<>();
        Restaurante rest = new Restaurante(fila);

        for (int i = 0; i < 100; i++){
            Cliente c = new Cliente(i, rest);
            Thread c_t = new Thread(c);
            c_t.start();
        }

        }
    }