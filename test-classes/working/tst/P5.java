class P5{

    public static void main(String []args){

    T t1;
    T t2;
    T t3;
    T t4;

    T a;
    T b;

    boolean p;
    boolean q;

    Buf l;

    t1 = new T();
    t2 = new T();
    t3 = new T();
    t4 = new T();

    p= true;
    q = false;

    if(p){
        a = t1;
    }else{
        a = t2;
    }

    if(q){
        b = t3;
    }else{
        b = t4;
    }

    l = new Buf();

    a.set(l);
    b.set(l);

    a.run();
    b.run();

    try {
        a.join();
        b.join();
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

public int add(int a, int b){
    return a + b;
}

public int some(){
    return 20;
}

public void run(){
    A obj;
    int a;
    int d;
    int res;

    try{
        synchronized(b){
            a = some(); 
        }

        a = 10;
        d = 20;
        res = add(a, d);
    }catch (Exception e){}

    }
}