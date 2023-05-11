package uk.woolhouse.pinreal;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {
    private final Photo[] dataset;

    public PhotoAdapter(QuerySnapshot snapshot) {
        ArrayList<Photo> list = new ArrayList<Photo>();
        for (QueryDocumentSnapshot document : snapshot) {
            list.add(new Photo(document.getId(), document.getString("img")));
        }
        dataset = list.toArray(new Photo[0]);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.photo_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.bind(dataset[position]);
    }

    @Override
    public int getItemCount() {
        return dataset.length;
    }

    public static class Photo {
        public final String uid;
        public final String img;
//        public final String owner;
//        public final String landmark;

        Photo(String uid, String img) {
            this.uid = uid;
            this.img = img;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView view_img;
        private final FirebaseStorage storage;
        private final File temp;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            storage = FirebaseStorage.getInstance();
            try {
                temp = File.createTempFile("photo", "img");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            view_img = (ImageView) view.findViewById(R.id.photo_img);
        }

        public void bind(@NonNull Photo photo) {
            storage.getReference(photo.img).getFile(temp).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(temp.getAbsolutePath());
                    view_img.setImageBitmap(bitmap);
                }
            });
        }
    }

}
