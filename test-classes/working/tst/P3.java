class P3 {
    public static void main(String []args){
        T t1;
        T t2;
        int b;
        int c;
        Buf l;

        try{
        l = new Buf();
        t1 = new T();
        t1.set1(l);
        t1.run();
        synchronized(l){
            b= 10;
            c =  20;
            c = l.add(b, c);
            l.notify();
        }
        t1.join();
    }catch (Exception e){}
        
    }
}


class Buf{
    public int add(int a, int b){
        return a + b;
    }

}

class C{


}

class T extends Thread{
    Buf b;

    public void set1(Buf k){
        b = k;
    }


    public void f1(C a){
        a = new C();
    }


    public void run(){
        try{

        C a;
        a = new C();
        synchronized(b){
            b.wait();
            this.f1(a);
        }
    }catch (Exception e){}

    }
}