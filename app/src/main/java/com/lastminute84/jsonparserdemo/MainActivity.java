package com.lastminute84.jsonparserdemo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lastminute84.jsonparserdemo.models.Movie;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setMessage("Loading. Please wait...");

        // Create default options which will be used for every
        //  displayImage(...) call if no options will be passed to this method
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config); // Do it on Application start

        listView = (ListView) findViewById(R.id.lvMovies);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if(id == R.id.action_refresh) {
            URL url = null;
            try {
                url = new URL("http://jsonparsing.parseapp.com/jsonData/moviesData.txt");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            new JSONTask().execute(url);
        }
        return super.onOptionsItemSelected(item);
    }

    public class JSONTask extends AsyncTask<URL, String, List<Movie>> {

        @Override
        protected List<Movie> doInBackground(URL... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {

                connection = (HttpURLConnection) params[0].openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";
                while((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                String finalJson = buffer.toString();
                Log.d("json", finalJson);
                JSONObject parentObject = new JSONObject(finalJson);
                JSONArray parentArray = parentObject.getJSONArray("movies");
                List<Movie> movies = new ArrayList<>();

                Gson gson = new Gson();
                for(int i = 0; i < parentArray.length(); i++) {
                    JSONObject finalObject = parentArray.getJSONObject(i);

                    Movie movie = gson.fromJson(finalObject.toString(), Movie.class);
                    /*movie.setMovie(finalObject.getString("movie"));
                    movie.setYear(finalObject.getInt("year"));
                    movie.setRating((float) finalObject.getDouble("rating"));
                    movie.setDirector(finalObject.getString("director"));
                    movie.setDuration(finalObject.getString("duration"));
                    movie.setTagLine(finalObject.getString("tagline"));
                    movie.setImage(finalObject.getString("image"));
                    movie.setStory(finalObject.getString("story"));

                    JSONArray castArray = finalObject.getJSONArray("cast");
                    List<Movie.Cast> castList = new ArrayList<>();
                    for(int j = 0; j < castArray.length(); j++) {
                        Movie.Cast cast = new Movie.Cast();
                        JSONObject castObject = castArray.getJSONObject(j);
                        cast.setName(castObject.getString("name"));
                        castList.add(cast);
                    }

                    movie.setCast(castList);*/
                    movies.add(movie);
                }

                Log.d("MOVIES LENGTH", "" + movies.size());
                return movies;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if(connection != null) {
                    connection.disconnect();
                }
                try {
                    if(reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.d("NULL-RETURN", "true");
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            super.onPostExecute(movies);
            dialog.dismiss();
            if(movies != null) {
                MovieAdapter adapter = new MovieAdapter(getApplicationContext(), R.layout.row, movies);
                listView.setAdapter(adapter);
            } else {
                new AlertDialog.Builder(getApplicationContext())
                        .setTitle("Network Error")
                        .setMessage("There is no connection")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }
    }

    public class MovieAdapter extends ArrayAdapter {

        private List<Movie> movieList;
        private int resource;
        private LayoutInflater inflater;

        public MovieAdapter(Context context, int resource, List<Movie> objects) {
            super(context, resource, objects);
            movieList = objects;
            this.resource = resource;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;

            if(convertView == null) {
                convertView = inflater.inflate(resource, null);
                holder = new ViewHolder();
                holder.ivMovieIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
                holder.tvMovie = (TextView) convertView.findViewById(R.id.tvMovie);
                holder.tvTagLine = (TextView) convertView.findViewById(R.id.tvTagline);
                holder.tvYear = (TextView) convertView.findViewById(R.id.tvYear);
                holder.tvDuration = (TextView) convertView.findViewById(R.id.tvDuration);
                holder.tvDirector = (TextView) convertView.findViewById(R.id.tvDirector);
                holder.rbMovieRating = (RatingBar) convertView.findViewById(R.id.rbMovie);
                holder.tvCast = (TextView) convertView.findViewById(R.id.tvCast);
                holder.tvStory = (TextView) convertView.findViewById(R.id.tvStory);
                holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);

            // Then later, when you want to display image
            ImageLoader.getInstance().displayImage(movieList.get(position).getImage(), holder.ivMovieIcon, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    progressBar.setVisibility(View.GONE);
                }
            });

            holder.tvMovie.setText(movieList.get(position).getMovie());
            holder.tvTagLine.setText(movieList.get(position).getTagLine());
            holder.tvYear.setText("Year: " + movieList.get(position).getYear());
            holder.tvDuration.setText("Duration: " + movieList.get(position).getDuration());
            holder.tvDirector.setText("Director: " + movieList.get(position).getDirector());
            holder.rbMovieRating.setRating(movieList.get(position).getRating()/2);

            StringBuffer buffer = new StringBuffer();
            for(Movie.Cast cast : movieList.get(position).getCast()) {
                buffer.append(cast.getName() + ", ");
            }
            holder.tvCast.setText("Cast: " + buffer);
            holder.tvStory.setText(movieList.get(position).getStory());


            return convertView;
        }

        class ViewHolder {
            ImageView ivMovieIcon;
            TextView tvMovie;
            TextView tvTagLine;
            TextView tvYear;
            TextView tvDuration;
            TextView tvDirector;
            RatingBar rbMovieRating;
            TextView tvCast;
            TextView tvStory;
            ProgressBar progressBar;
        }
    }
}
