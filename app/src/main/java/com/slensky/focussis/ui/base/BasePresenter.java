package com.slensky.focussis.ui.base;

import javax.inject.Inject;

/**
 * Base class that implements the UserActions interface and provides a base implementation for
 * onAttach() and onDetach(). It also handles keeping a reference to the view that
 * can be accessed from the children classes by calling getView().
 */
public abstract class BasePresenter<V extends MvpView> implements MvpPresenter<V> {

    protected V view;

    @Override
    public void onAttach(V view) {
        this.view = view;
    }

    @Override
    public void onDetach() {
        view = null;
    }

    public boolean isViewAttached() {
        return view != null;
    }

    public V getView() {
        return view;
    }

}