class P4 {
    public static void main(String []args){
        T t1;
        T t2;
        T t3;
        Buf l;
        l = new Buf();
        t1 = new T();
        t2 = new T();
        t3 = new T();
        t1.set(l);
        t2.set(l);
        t3.set(l);
        t1.run();
        t2.run();
        t3.run();
        synchronized(l){
            l.notify();
            l.notifyAll();
        }
        synchronized(l){
            l.notifyAll();
            l.notify();
        }

        try {
            t1.join();
            t2.join();
            t3.join();
        }catch (Exception e){}
    }
}

class Buf{

}


class T extends Thread{
    Buf b;

    public void set(Buf k){
        b = k;
    }


    public int f1(int a){
        return a + 50;
    }


    public int f2(int a){
        return a + 20;
    }

    public void run(){
        try{
            int a;

            a =10;
            synchronized(b){
                b.wait();
                a = f1(a);
            }
            synchronized(b){
                b.wait();
                a = f2(a);
            }
        }catch(Exception e){}

    }
}