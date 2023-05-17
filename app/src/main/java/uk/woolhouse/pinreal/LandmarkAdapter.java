package uk.woolhouse.pinreal;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import uk.woolhouse.pinreal.model.Landmark;

public class LandmarkAdapter extends RecyclerView.Adapter<LandmarkAdapter.ViewHolder> {
    private final Landmark[] dataset;
    private final Database db;

    public LandmarkAdapter(Database db, Location location, List<Landmark> landmarks) {
        this.db = db;
        landmarks.sort((o1, o2) -> Float.compare(location.distanceTo(o1.location()), location.distanceTo(o2.location())));
        dataset = landmarks.toArray(new Landmark[0]);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        var view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.landmark_item, viewGroup, false);
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
        private Landmark current;
        private TextView view_name;

        public ViewHolder(View view) {
            super(view);
            view_name = (TextView) view.findViewById(R.id.adapter_landmark_name);
            var view_location = (Button) view.findViewById(R.id.adapter_landmark_loc);
            var view_layout = (ConstraintLayout) view.findViewById(R.id.adapter_landmark_layout);

            view_location.setOnClickListener(v -> {
                var ctx = v.getContext();
                var intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(Locale.UK, "geo:%f,%f", current.location().getLatitude(), current.location().getLongitude())));
                if (intent.resolveActivity(ctx.getPackageManager()) != null) {
                    ctx.startActivity(intent);
                }
            });
            view_layout.setOnClickListener(v -> {
                v.getContext().startActivity(LandmarkActivity.From(v.getContext(), current.uuid()));
            });
        }

        public void bind(@NonNull Landmark landmark, Database db) {
            current = landmark;
            view_name.setText(landmark.name());
        }
    }

}
