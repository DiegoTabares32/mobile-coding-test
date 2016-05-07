package com.example.mobilecodingtest.Interface;

import com.example.mobilecodingtest.Model.WeaponsLocation;

import java.util.List;

/**
 * Created by Diego on 07/05/2016.
 */
public interface LocationsListener {

    public void onError(String error);

    public void onResponse(List<WeaponsLocation> response);
}
