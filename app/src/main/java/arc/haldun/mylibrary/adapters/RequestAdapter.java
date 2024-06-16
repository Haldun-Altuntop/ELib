package arc.haldun.mylibrary.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import arc.haldun.database.database.Manager;
import arc.haldun.database.database.MariaDB;
import arc.haldun.database.exception.OperationFailedException;
import arc.haldun.database.objects.Book;
import arc.haldun.database.objects.CurrentUser;
import arc.haldun.database.objects.Request;
import arc.haldun.database.objects.User;
import arc.haldun.mylibrary.R;

public class RequestAdapter
        extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder>
        implements View.OnClickListener {

    private final ArrayList<Request> requests;

    public RequestAdapter(ArrayList<Request> requests) {
        this.requests = requests;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.card_request, viewGroup, false);

        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder requestViewHolder, int i) {

        Request currentRequest = requests.get(i);
        requestViewHolder.setData(currentRequest);

        requestViewHolder.itemView.setOnClickListener(this);
        requestViewHolder.btn_accept.setOnClickListener(v -> {
            requestViewHolder.acceptRequest();
            removeItem(requestViewHolder.getAdapterPosition());
        });
        requestViewHolder.btn_reject.setOnClickListener(v -> {
            requestViewHolder.rejectRequest();
            removeItem(requestViewHolder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    @Override
    public void onClick(View v) {

    }

    private void createInformationDialog(View v) {

        StringBuilder stringBuilder = new StringBuilder();

        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());


    }

    public void addItem(Request request) {
        requests.add(request);
        notifyItemInserted(requests.size() - 1);
    }

    public void removeItem(int position) {
        requests.remove(position);
        notifyItemRemoved(position);
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tv_owner, tv_requestedBook;
        private ImageButton btn_accept, btn_reject;
        private static final Manager databaseManager = new Manager(new MariaDB());
        private Request request;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            init(itemView);
        }

        private void init(View v) {
            tv_owner = v.findViewById(R.id.card_request_tv_owner);
            tv_requestedBook = v.findViewById(R.id.card_request_tv_requested_book);

            btn_accept = v.findViewById(R.id.card_request_btn_accept);
            btn_reject = v.findViewById(R.id.card_request_btn_reject);
        }

        public void setData(Request request) {

            this.request = request;
            User requestOwner = request.getOwner();
            Book requestedBook = request.getRequestedBook();

            this.tv_owner.setText(requestOwner.getName());
            this.tv_requestedBook.setText(
                    requestedBook.getName()
            );
        }

        @Override
        public void onClick(View v) {
            if (v.equals(btn_accept)) acceptRequest();
            if (v.equals(btn_reject)) rejectRequest();
        }

        private void acceptRequest() {

            new Thread(() -> {
                try {
                    databaseManager.acceptRequest(request, CurrentUser.user);
                } catch (OperationFailedException e) {
                    throw new RuntimeException(e);
                }
            }).start();


        }

        private void rejectRequest() {
            new Thread(()-> {
                databaseManager.rejectRequest(request, CurrentUser.user);
            }).start();
        }
    }
}
