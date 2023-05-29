package jr.project.cloudbox.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

import jr.project.cloudbox.R;
import jr.project.cloudbox.utils.Extras;

public class PdfViewerActivity extends AppCompatActivity {

    PDFView pdfView;
    AlertDialog.Builder builder;
    String password = null;
    String pdfUrl,pdfName;
    CardView progress;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);
        Objects.requireNonNull(getSupportActionBar()).hide();

        StrictMode.VmPolicy.Builder pBuild = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(pBuild.build());

        builder = new AlertDialog.Builder(this);

        pdfUrl  = getIntent().getStringExtra("url");
        pdfName = getIntent().getStringExtra("name");

        if (URLUtil.isValidUrl(pdfUrl)){
            if (!Extras.urlExist(pdfUrl)){
                Toast.makeText(
                                this,
                                "File has been removed from server!",
                                Toast.LENGTH_SHORT
                        )
                        .show();
                finish();
            }
        }

        pdfView  = findViewById(R.id.pdfView);
        progress = findViewById(R.id.progressCard);

        TextView name = findViewById(R.id.fileName);

        name.setText(pdfName+"");

        new RetrievePdfFromUrl().execute(pdfUrl);

        findViewById(R.id.imageView20).setOnClickListener(v -> {
            finish();
        });


    }

    public void openOptionMenu(View v){
        PopupMenu popup = new PopupMenu(v.getContext(), v);
        popup.getMenuInflater().inflate(R.menu.recent_recycler_drop_down, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            return true;
        });
        popup.show();
    }

    public void showPasswordDialog(){

        builder.setTitle("Enter Password");

        // set the custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.et_dialog,null);
        builder.setView(customLayout);

        builder.setPositiveButton("OK", (dialog, which) -> {
                    EditText editText = customLayout.findViewById(R.id.editText);
                    password = editText.getText().toString();
                    new RetrievePdfFromUrl().execute(pdfUrl);
                });
        builder.setNegativeButton("CANCEL", (dialog, which) -> {
            Toast.makeText(this,
                    "This pdf is password protected. Please enter the password to continue",
                    Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            finish();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    void dialog1(int visibility){
        runOnUiThread(() -> progress.setVisibility(visibility));
    }


    class RetrievePdfFromUrl extends AsyncTask<String, Void, InputStream> {
        @Override
        protected InputStream doInBackground(String... strings) {
            dialog1(View.VISIBLE);
            InputStream inputStream = null;
            try {
                URL url = new URL(strings[0]);
                // creating our connection.
                HttpURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                if (urlConnection.getResponseCode() == 200) {
                    // we are getting input stream from url
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            return inputStream;
        }

        @Override
        protected void onPostExecute(InputStream inputStream) {
            // we are loading our pdf in our pdf view.
            pdfView.fromStream(inputStream)
                    .password(password)
                    .onLoad(nbPages -> dialog1(View.GONE))
                    .onError(t -> {
                if(t.getMessage()!=null){
                    if (t.getMessage().equals("Password required or incorrect password.")){
                        if (password == null) {
                            Toast.makeText(PdfViewerActivity.this,
                                    "This pdf is password protected. Please enter the password to continue",
                                    Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(PdfViewerActivity.this,
                                    "Incorrect password! Please try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        showPasswordDialog();
                    }else {
                        Toast.makeText(PdfViewerActivity.this,
                                ""+t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
                dialog1(View.GONE);
            }).load();
        }
    }
}