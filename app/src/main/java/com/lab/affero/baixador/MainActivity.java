package com.lab.affero.baixador;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DecimalFormat;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSION = 1;
    Button button;
    ProgressDialog mprogressDialog;
    double file_size = 0;
    String file_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION);
                } else {
                    File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MeusTreinamentos/");
                    try {
                        dir.mkdir();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Não foi possível cria a pasta!", Toast.LENGTH_SHORT).show();
                    }
                    new DownloadTask().execute("https://agenciasupermix.top/apoio/teste.txt");
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permissão concedida", Toast.LENGTH_SHORT).show();

                    File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MeusTreinamentos/");
                    try {
                        dir.mkdir();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Não foi possível cria a pasta!", Toast.LENGTH_SHORT).show();
                    }

                    new DownloadTask().execute("https://agenciasupermix.top/apio/aula-temp.mp4");

                } else {
                    Toast.makeText(this, "Permissão Negada", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {
            file_name = strings[0].substring(strings[0].lastIndexOf("/") + 1);
            try {
                InputStream input = null;
                OutputStream output = null;
                HttpsURLConnection connection = null;
                try {
                    URL url = new URL(strings[0]);
                    connection = (HttpsURLConnection) url.openConnection();
                    connection.connect();
                    if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                        return "O Servidor retornou HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
                    }

                    int fileLength = connection.getContentLength();
                    file_size = fileLength;
                    input = connection.getInputStream();
                    output = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MeusTreinamentos/" + file_name);

                    byte data[] = new byte[4096];
                    long total = 0;
                    int count;

                    while ((count = input.read(data)) != -1) {
                        if (isCancelled()) {
                            return null;
                        }
                        total += count;
                        if (fileLength > 0) {
                            publishProgress((int) (total * 100 / fileLength));
                            output.write(data, 0, count);
                        }
                    }

                } catch (Exception e) {
                    return e.toString();
                } finally {
                    try {
                        if (output != null) {
                            output.close();
                        }

                        if (input != null) {
                            input.close();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (connection != null) {
                        connection.disconnect();
                    }
                }

                try {
                    URL url = new URL(strings[0]);
                } catch (Exception e) {
                    return e.toString();
                } finally {
                    try {
                        if (output != null) {
                            output.close();
                        }

                        if (input != null) {
                            input.close();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            } finally {
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mprogressDialog = new ProgressDialog(MainActivity.this);
            mprogressDialog.setTitle("Baixando o conteúdo...");
            mprogressDialog.setMessage("Tamanho do download: 0 MB");
            mprogressDialog.setIndeterminate(true);
            mprogressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mprogressDialog.setCancelable(true);
            mprogressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {

                    Toast.makeText(MainActivity.this, "Download cancelado!", Toast.LENGTH_SHORT).show();
                    File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MeusTreinamentos/" + file_name);

                    try {
                        dir.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            mprogressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mprogressDialog.setIndeterminate(true);
            mprogressDialog.setMax(100);
            mprogressDialog.setProgress(values[0]);
            mprogressDialog.setMessage("Tamanho: " + new DecimalFormat("##.##").format(file_size / 1000000) + " MB");
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            mprogressDialog.dismiss();

            if (result != null) {
                Toast.makeText(MainActivity.this, "Erro: " + result, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Download completo!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}