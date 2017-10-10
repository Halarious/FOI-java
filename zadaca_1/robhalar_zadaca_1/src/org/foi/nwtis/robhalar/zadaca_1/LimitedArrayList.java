package org.foi.nwtis.robhalar.zadaca_1;

import java.util.ArrayList;

public class LimitedArrayList<T> extends ArrayList<T>
{
    private int maxSize;
    
    public LimitedArrayList(int capacity)
    {
        this.maxSize = capacity;
    }

    @Override
    public void add(int index, T element)
    {
        if (isFull()                || 
            index        >  maxSize ||
            get(maxSize) != null
           )
            return;
        super.add(index, element);
    }

    @Override
    public boolean add(T element)
    {
        if (isFull())
            return false;
        return super.add(element); 
    }
    
    public boolean isFull()
    {
        return this.size() == maxSize;
    }
}
