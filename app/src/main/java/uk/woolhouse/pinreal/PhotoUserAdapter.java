package uk.woolhouse.pinreal;

import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import uk.woolhouse.pinreal.model.Photo;

public class PhotoUserAdapter extends RecyclerView.Adapter<PhotoUserAdapter.ViewHolder> {
    private final Photo[] dataset;
    private final Database db;

    public PhotoUserAdapter(Database db, QuerySnapshot snapshot) {
        this.db = db;
        ArrayList<Photo> list = new ArrayList<>();
        for (QueryDocumentSnapshot document : snapshot) {
            list.add(Photo.From(document));
        }
        dataset = list.toArray(new Photo[0]);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        var view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.photo_user_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.bind(dataset[position], db);
    }

    @Override
    public int getItemCount() {
        return dataset.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView view_img;
        private final Button view_btn;
        private Photo current;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            view_img = view.findViewById(R.id.photo_user_img);
            view_btn = view.findViewById(R.id.photo_user_landmark_btn);
            view_btn.setOnClickListener(v -> {
                v.getContext().startActivity(LandmarkActivity.From(v.getContext(), current.landmark()));
            });
            view_img.setOnClickListener(v -> {
                v.getContext().startActivity(PhotoActivity.From(v.getContext(), current.uuid()));
            });
        }

        public void bind(@NonNull Photo photo, Database db) {
            current = photo;
            db.img(photo.img(), file -> {
                X.SetImage(view_img, file);
            });
            db.landmark(photo.landmark(), landmark -> {
                view_btn.setText(landmark.name());
            });
        }
    }

}
