package com.etbakhly_provider.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.databinding.DataBindingUtil;

import com.etbakhly_provider.R;

import com.etbakhly_provider.databinding.SpinnerRowBinding;
import com.etbakhly_provider.model.CountryModel;

import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;

public class SpinnerItemAdapter extends BaseAdapter {
    private List<Object> dataList;
    private Context context;
    private String lang;

    public SpinnerItemAdapter(List<Object> dataList, Context context) {
        this.dataList = dataList;
        this.context = context;
        Paper.init(context);
        lang = Paper.book().read("lang", Locale.getDefault().getLanguage());
    }

    @Override
    public int getCount() {
        if (dataList == null)
            return 4;
        else
            return dataList.size();
    }

    @Override
    public Object getItem(int i) {
        return dataList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        @SuppressLint("ViewHolder") SpinnerRowBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.spinner_row, viewGroup, false);
        binding.setTitle(lang);
//        binding.setModel(dataList.get(i));
        return binding.getRoot();
    }

    public void updateData(List<Object> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }
}
