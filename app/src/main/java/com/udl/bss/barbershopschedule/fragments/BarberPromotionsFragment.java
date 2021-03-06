package com.udl.bss.barbershopschedule.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.udl.bss.barbershopschedule.R;
import com.udl.bss.barbershopschedule.adapters.PromotionAdapter;
import com.udl.bss.barbershopschedule.domain.Barber;
import com.udl.bss.barbershopschedule.domain.Promotion;
import com.udl.bss.barbershopschedule.listeners.PromotionClick;
import com.udl.bss.barbershopschedule.serverCommunication.APIController;

import java.util.ArrayList;
import java.util.List;


public class BarberPromotionsFragment extends Fragment {


    private RecyclerView promotionsRecyclerView;
    private PromotionAdapter adapter;
    private Barber barber;
    private SharedPreferences mPrefs;

    private OnFragmentInteractionListener mListener;

    public BarberPromotionsFragment() {}

    public static BarberPromotionsFragment newInstance() {
        return new BarberPromotionsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_barber_promotions, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getActivity() != null) {
            Fade fade = new Fade();
            fade.excludeTarget(android.R.id.statusBarBackground, true);
            fade.excludeTarget(android.R.id.navigationBarBackground, true);

            getActivity().getWindow().setEnterTransition(fade);
            getActivity().getWindow().setExitTransition(fade);
        }

        mPrefs = getActivity().getSharedPreferences("USER", Activity.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString("user", "");
        barber = gson.fromJson(json, Barber.class);


        if (getView() != null) {
            promotionsRecyclerView = getView().findViewById(R.id.rv);
        }

        if (promotionsRecyclerView != null) {

            promotionsRecyclerView.setHasFixedSize(true);
            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            promotionsRecyclerView.setLayoutManager(llm);
            promotionsRecyclerView.setAdapter(
                    new PromotionAdapter(new ArrayList<Promotion>(), null, null));
            setPromotions();

            /* Swipe down to refresh */
            final SwipeRefreshLayout sr = getView().findViewById(R.id.swiperefresh);
            sr.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    sr.setRefreshing(false);

                    if (adapter == null) {
                        adapter = (PromotionAdapter) promotionsRecyclerView.getAdapter();

                    }
                    adapter.removeAll();
                    setPromotions();
                }
            });
            sr.setColorSchemeResources(android.R.color.holo_blue_dark,
                    android.R.color.holo_green_dark,
                    android.R.color.holo_orange_dark,
                    android.R.color.holo_red_dark);
            /* /Swipe down to refresh */

        }
    }

    private void setPromotions() {

        if (barber != null) {
            APIController.getInstance().getPromotionsByBarber(barber.getToken(), String.valueOf(barber.getId()))
                    .addOnCompleteListener(new OnCompleteListener<List<Promotion>>() {
                @Override
                public void onComplete(@NonNull Task<List<Promotion>> task) {
                    PromotionAdapter adapter = new PromotionAdapter(
                            task.getResult(),
                            new PromotionClick(getActivity(), promotionsRecyclerView),
                            barber.getToken());
                    promotionsRecyclerView.setAdapter(adapter);
                }
            });
        }
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
