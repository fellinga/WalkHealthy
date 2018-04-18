package fighting_mongooses.walkhealthy.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fighting_mongooses.walkhealthy.R;
import fighting_mongooses.walkhealthy.objects.ChatData;

/**
 * Class responsible to show all the messages
 * in the chat
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    /**
     * ViewHolder to be the item of the list
     */
    static final class ChatViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView message;
        TextView date;

        ChatViewHolder(View view) {
            super(view);

            name = view.findViewById(R.id.item_username);
            message = view.findViewById(R.id.item_message);
            date = view.findViewById(R.id.item_date);
        }
    }

    private List<ChatData> mContent = new ArrayList<>();

    public void clearData() {
        mContent.clear();
    }

    public void addData(ChatData data) {
        mContent.add(data);
    }

    @Override
    public int getItemCount() {
        return mContent.size();
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chat, parent, false);
        return new ChatViewHolder(root);
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        ChatData data = mContent.get(position);

        holder.message.setText(data.getMessage());
        holder.name.setText(data.getName());
        Date d = new Date();
        d.setTime(data.getDate());
        holder.date.setText(String.valueOf(d).replace(" EDT", ""));
    }
}
