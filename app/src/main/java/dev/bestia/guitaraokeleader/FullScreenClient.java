package dev.bestia.guitaraokeleader;

import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.widget.RelativeLayout;

public class FullScreenClient extends WebChromeClient {

    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);

    ViewGroup parent;
    ViewGroup content;
    View customView;

    public FullScreenClient(ViewGroup parent, ViewGroup content){
        this.parent = parent;
        this.content = content;
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        super.onShowCustomView(view, callback);

        customView = view;
        view.setLayoutParams(layoutParams);
        parent.addView(view);
        content.setVisibility(View.GONE);
    }

    @Override
    public void onHideCustomView() {
        super.onHideCustomView();

        content.setVisibility(View.VISIBLE);
        parent.removeView(customView);
        customView = null;
    }
}