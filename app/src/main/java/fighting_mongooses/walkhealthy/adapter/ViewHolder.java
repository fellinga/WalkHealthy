package fighting_mongooses.walkhealthy.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import fighting_mongooses.walkhealthy.R;

/**
 * Created by mario on 3/22/2018.
 */

public class ViewHolder extends RecyclerView.ViewHolder {

    public LinearLayout linearLayout;

    private TextView title;
    private TextView description;
    private ImageView imageView;

    public ViewHolder(View itemView) {
        super(itemView);
        this.linearLayout = (LinearLayout) itemView.findViewById(R.id.linearLayout);
        this.title = (TextView)itemView.findViewById(R.id.row_title);
        this.description = (TextView)itemView.findViewById(R.id.row_description);
        this.imageView = (ImageView) itemView.findViewById(R.id.row_image);
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public void setDescription(String description) {
        this.description.setText(description);
    }

    public void setImageView(int imageId) {
        this.imageView.setImageResource(imageId);
    }

}