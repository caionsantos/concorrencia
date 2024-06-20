import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {

    public static class Carro{

        public boolean direcao;
        public int id;
        public Carro(boolean direcao, int id){
            this.direcao = direcao;
            this.id = id;
        }
    }
    public static class Ponte{
        private final Lock lock = new ReentrantLock();

        public void entrar(Carro carro) throws InterruptedException {
            System.out.println("Carro " + carro.id + " tenta entrar pela " + carro.direcao);
            lock.lock();
            System.out.println("Carro " + carro.id + " está passando");
            Thread.sleep(1000);
            System.out.println("Carro " + carro.id + " está saindo");
            lock.unlock();
        }
    }

    public static class FioCarro implements Runnable{
        public Carro carro;
        public Ponte ponte;

        public FioCarro(Carro carro, Ponte ponte){

            this.carro = carro;
            this.ponte = ponte;
        }

        public void run() {
            try {
                ponte.entrar(carro);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {

        Ponte ponte = new Ponte();
        //True = esquerda, False = direita
        Carro carro1 = new Carro(true, 1);
        Carro carro2 = new Carro(true, 2);
        Carro carro3 = new Carro(false, 3);
        Carro carro4 = new Carro(true, 4);
        Carro carro5 = new Carro(false, 5);
        Carro carro6 = new Carro(false, 6);
        Carro carro7 = new Carro(false, 7);
        Carro carro8 = new Carro(true, 8);

        FioCarro FC1 = new FioCarro(carro1, ponte);
        FioCarro FC2 = new FioCarro(carro2, ponte);
        FioCarro FC3 = new FioCarro(carro3, ponte);
        FioCarro FC4 = new FioCarro(carro4, ponte);
        FioCarro FC5 = new FioCarro(carro5, ponte);
        FioCarro FC6 = new FioCarro(carro6, ponte);
        FioCarro FC7 = new FioCarro(carro7, ponte);
        FioCarro FC8 = new FioCarro(carro8, ponte);

        Thread FC1_T = new Thread(FC1);
        Thread FC2_T = new Thread(FC2);
        Thread FC3_T = new Thread(FC3);
        Thread FC4_T = new Thread(FC4);
        Thread FC5_T = new Thread(FC5);
        Thread FC6_T = new Thread(FC6);
        Thread FC7_T = new Thread(FC7);
        Thread FC8_T = new Thread(FC8);

        FC1_T.start();
        FC2_T.start();
        FC3_T.start();
        FC4_T.start();
        FC5_T.start();
        FC6_T.start();
        FC7_T.start();
        FC8_T.start();

    }
}
