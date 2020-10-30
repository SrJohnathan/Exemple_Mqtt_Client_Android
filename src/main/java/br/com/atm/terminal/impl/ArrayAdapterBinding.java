package br.com.atm.terminal.impl;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import br.com.atm.terminal.databinding.SpinnerBinding;

public class ArrayAdapterBinding<T> extends ArrayAdapter<T> {

   private   Context context;



    public ArrayAdapterBinding(@NonNull Context context, int resource) {
        super(context, resource);
    }




    public  void setItems(@NonNull Context context,T... items){
        this.context = context;
        this.addAll(items);
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        SpinnerBinding binding = SpinnerBinding.inflate(layoutInflater);

        return super.getView(position, binding.getRoot(), parent);
    }
}
