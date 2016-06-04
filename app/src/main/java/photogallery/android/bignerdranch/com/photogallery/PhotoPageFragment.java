package photogallery.android.bignerdranch.com.photogallery;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

public class PhotoPageFragment extends VisibleFragment
{
    private String url;
    private WebView webView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        url = getActivity().getIntent().getData().toString();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_photo_page, parent, false);

        final ProgressBar progressBar = (ProgressBar)v.findViewById(R.id.progressBar);

        progressBar.setMax(100);

        final TextView titleTextView = (TextView)v.findViewById(R.id.titleTextView);



        webView = (WebView)v.findViewById(R.id.webView);

        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient( new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView newView, String url)
            {
                return  false;
            }

        });

        webView.loadUrl(url);

        webView.setWebChromeClient( new WebChromeClient() {
            public void onProgressChanged(WebView newWebView, int progress)
            {
                if ( progress == 100 )
                {
                    progressBar.setVisibility(View.INVISIBLE);

                }
                else
                {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(progress);
                }
            }

            public void onReceivedTitle(WebView newWebView, String title)
            {
                titleTextView.setText(title);
            }
        });

        return v;
    }
}
