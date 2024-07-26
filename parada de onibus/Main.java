import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;

public class Main {

    public static class Passageiro implements Runnable{

        int id;
        Parada parada;

        Passageiro(int id, Parada parada){
            this.id = id;
            this.parada = parada;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(new Random().nextInt(10000));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Passageiro " + this.id + " chegou na parada");
            try {
                this.parada.entrar(this);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Onibus implements Runnable{

        int i;
        Parada parada;

        Onibus(Parada parada){
            this.parada = parada;
            this.i = 0;
        }

        void saida(){
            try {
                Thread.sleep(new Random().nextInt(2000) + 1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                this.i += 1;
                if (i < 9) {
                    this.parada.chegar(this.i, this);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            this.saida();
        }
    }

    public static class Parada{

        int sentado;
        int esperando;
        private final Lock n_assentos = new ReentrantLock();
        private final Lock n_espera = new ReentrantLock();
        private final Lock chegou = new ReentrantLock();
        private final Lock sinc = new ReentrantLock();
        private final Condition onibus_chega = sinc.newCondition();
        private final Semaphore entrar_onibus = new Semaphore(50);

        public void entrar(Passageiro passageiro) throws InterruptedException {
            chegou.lock();
            System.out.println("Passageiro " + passageiro.id + " está esperando o ônibus");
            chegou.unlock();
            n_espera.lock();
            this.esperando += 1;
            n_espera.unlock();
            sinc.lock();
            onibus_chega.await();
            if (entrar_onibus.tryAcquire(1)) {
                System.out.println("Passageiro " + passageiro.id + " entrou no ônibus");
                this.esperando -= 1;
                if (this.esperando == 0) {
                    onibus_chega.signalAll();
                }
                this.sentado += 1;
                sinc.unlock();
            } else {
                this.esperando -= 1;
                System.out.println("Passageiro " + passageiro.id + " não conseguiu subir");
                onibus_chega.signalAll();
                sinc.unlock();
                this.entrar(passageiro);
            }
        }

        public void chegar(int i, Onibus self) throws InterruptedException {
            chegou.lock();
            sinc.lock();
            System.out.println("Ônibus " + i + " chegou");
            onibus_chega.signalAll();
            onibus_chega.await(4, TimeUnit.SECONDS);
            System.out.println("Ônibus " + i + " saiu com " + this.sentado + " passageiros");
            onibus_chega.signalAll();
            sinc.unlock();
            n_assentos.lock();
            entrar_onibus.release(this.sentado);
            this.sentado = 0;
            n_assentos.unlock();
            chegou.unlock();
            self.saida();
        }

        Parada(){
            this.esperando = 0;
            this.sentado = 0;
        }

    }


    public static void main(String[] args) {
        Parada parada = new Parada();
        Onibus onibus = new Onibus(parada);
        Thread o = new Thread(onibus);
        for (int i = 0; i < 100; i++){
            Passageiro p = new Passageiro(i, parada);
            Thread p_t = new Thread(p);
            p_t.start();
        }
        o.start();
    }
}
