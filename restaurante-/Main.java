import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.LinkedList;
import java.util.Random;

public class Main {

    public static class Restaurante{
        private final Semaphore vagas = new Semaphore(5);
        private Lock trava_fila = new ReentrantLock();
        private Lock trava_num = new ReentrantLock();
        public int espaco;
        public boolean cheio;

        public Restaurante(){
            this.cheio = false;
            this.espaco = 0;
        }

    }

    public static class Cliente implements Runnable{
        public int id;
        public Restaurante rest;
        public LinkedList<Cliente> fila;
;

        public Cliente(int id, Restaurante rest, LinkedList<Cliente> fila){
            this.id = id;
            this.rest = rest;
            this.fila = fila;
        }

        @Override
        public void run() {
            try {
                //rest.comer();
                Thread.sleep(new Random().nextInt(10000)); // simulando tempo pra chegar, variabilidade alta ajuda a demonstrar o algoritmo
                System.out.println("Cliente " + this.id + " está tentando se sentar");
                rest.vagas.acquire();
                rest.trava_num.lock();
                rest.espaco += 1;
                if (rest.espaco == 5){
                    rest.cheio = true;
                }
                rest.trava_num.unlock();
                System.out.println("Cliente " + this.id + " está comendo");
                //Thread.sleep(1000);
                Thread.sleep(new Random().nextInt(1000));
                System.out.println("Cliente " + this.id + " está saindo");
                rest.trava_num.lock();
                rest.espaco -= 1;
                if (rest.cheio){
                    if (rest.espaco == 0){
                        rest.cheio = false;
                        rest.vagas.release(5);
                    }
                    rest.trava_num.unlock();
                }
                else {
                    rest.trava_num.unlock();
                    rest.vagas.release();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        Restaurante rest = new Restaurante();
        LinkedList<Cliente> fila = new LinkedList<>();

        for (int i = 0; i < 100; i++){
            Cliente c = new Cliente(i, rest, fila);
            Thread c_t = new Thread(c);
            c_t.start();
        }

        }
    }