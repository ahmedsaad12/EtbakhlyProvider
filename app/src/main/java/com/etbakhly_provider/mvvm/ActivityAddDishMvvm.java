package com.etbakhly_provider.mvvm;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;


import com.etbakhly_provider.model.AddDishDataModel;
import com.etbakhly_provider.model.AddDishModel;
import com.etbakhly_provider.remote.Api;
import com.etbakhly_provider.share.Common;
import com.etbakhly_provider.tags.Tags;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

public class ActivityAddDishMvvm extends AndroidViewModel {

    private MutableLiveData<Boolean> addDishLiveData;


    private CompositeDisposable disposable = new CompositeDisposable();

    public ActivityAddDishMvvm(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<Boolean> getAddDishLiveData() {
        if (addDishLiveData==null){
            addDishLiveData=new MutableLiveData<>();
        }
        return addDishLiveData;
    }

    public void storeDish(Context context, AddDishModel addDishModel, Uri uri){
        RequestBody titel= Common.getRequestBodyText(addDishModel.getTitel());
        RequestBody qty= Common.getRequestBodyText(addDishModel.getQty());
        RequestBody price= Common.getRequestBodyText(addDishModel.getTitel());
        RequestBody category_dishes_id= Common.getRequestBodyText(addDishModel.getCategory_dishes_id()+"");
        RequestBody caterer_id= Common.getRequestBodyText("27");
        RequestBody details=Common.getRequestBodyText(addDishModel.getDetails());

        MultipartBody.Part image=Common.getMultiPart(context,uri,"photo");

        Api.getService(Tags.base_url).storeDish(titel,category_dishes_id,price,details,image,qty,caterer_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Response<AddDishDataModel>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull Response<AddDishDataModel> response) {
                        if (response.isSuccessful()){
                            if (response.body().getStatus()==200){
                                addDishLiveData.postValue(true);
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.d("error", e.getMessage());
                    }
                });
    }
}
