import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {

    public static class Conta{
        public int saldo;
        private final Lock lock = new ReentrantLock();

        public Conta(int valor){
            this.saldo = valor;
        }

        public void deposito(int add) throws InterruptedException {
            System.out.println("Depósito " + add + " acessando...");
            lock.lock();
            saldo += add;
            System.out.println("depositado:" + add);
            lock.unlock();
        }

        public void saque(int remove) throws InterruptedException {
            System.out.println("Saque " + remove + " acessando...");
            if (remove > saldo){
                System.out.println("ERRO: Saldo Indisponível");
            }else{
                lock.lock();
                if (remove > saldo){
                    System.out.println("ERRO: Saldo Indisponível");
                    lock.unlock();
                }else {
                    saldo -= remove;
                    System.out.println("sacado:" + remove);
                    lock.unlock();
                }
            }
        }
    }

    public static class FioDeposito implements Runnable{
        public Conta conta;
        public int add;

        public FioDeposito(Conta conta, int add){
            this.conta = conta;
            this.add = add;
        }

        public void run() {
            try {
                conta.deposito(add);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class FioSaque implements Runnable{
        public Conta conta;
        public int remove;

        public FioSaque(Conta conta, int remove){
            this.conta = conta;
            this.remove = remove;
        }

        public void run() {
            try {
                conta.saque(remove);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Valor inicial: 0 reais");

        Conta contafamiliar = new Conta(0);

        FioDeposito Dep1 = new FioDeposito(contafamiliar, 500);
        FioDeposito Dep2 = new FioDeposito(contafamiliar, 200);
        FioSaque Saq1 = new FioSaque(contafamiliar, 500);
        FioSaque Saq2 = new FioSaque(contafamiliar, 1000);
        FioDeposito Dep3 = new FioDeposito(contafamiliar, 2000);
        FioSaque Saq3 = new FioSaque(contafamiliar, 1000);
        FioDeposito Dep4 = new FioDeposito(contafamiliar, 300);
        FioSaque Saq4 = new FioSaque(contafamiliar, 1000);
        //valor final: 500

        Thread Dep1_T = new Thread(Dep1);
        Thread Dep2_T = new Thread(Dep2);
        Thread Dep3_T = new Thread(Dep3);
        Thread Dep4_T = new Thread(Dep4);

        Thread Saq1_T = new Thread(Saq1);
        Thread Saq2_T = new Thread(Saq2);
        Thread Saq3_T = new Thread(Saq3);
        Thread Saq4_T = new Thread(Saq4);

        Dep1_T.start();
        //Dep1_T.join();
        Dep2_T.start();
        //Dep2_T.join();
        Saq1_T.start();
        //Saq1_T.join();
        Saq2_T.start();
        //Saq2_T.join();
        Dep3_T.start();
        //Dep3_T.join();
        Saq3_T.start();
        //Saq3_T.join();
        Dep4_T.start();
        //Dep4_T.join();
        Saq4_T.start();
        //Saq4_T.join();

        Dep1_T.join();
        Dep2_T.join();
        Saq1_T.join();
        Saq2_T.join();
        Dep3_T.join();
        Saq3_T.join();
        Dep4_T.join();
        Saq4_T.join();

        System.out.println("Valor final: " + contafamiliar.saldo);

        }
    }