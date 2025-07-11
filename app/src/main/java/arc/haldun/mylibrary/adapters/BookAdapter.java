package arc.haldun.mylibrary.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import arc.haldun.database.objects.Book;
import arc.haldun.database.objects.CurrentUser;
import arc.haldun.database.objects.User;
import arc.haldun.mylibrary.R;
import arc.haldun.mylibrary.main.BookDetailsActivity;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.MyViewHolder> {

    ArrayList<Book> books;
    LayoutInflater layoutInflater;
    Activity rootActivity;
    PositionChangeListener positionChangeListener;

    /**
     * Bu yapıcı metot tek bir kitabı listelemek için kullanılır.
     * @param activity Adaptörün kök aktivitesi
     * @param book Listelenecek kitap
     */
    public BookAdapter(Activity activity, Book book) {
        this.books = new ArrayList<>(Collections.singletonList(book)); // TODO: test this changesets
        this.layoutInflater = LayoutInflater.from(activity.getApplicationContext());
        this.rootActivity = activity;
    }

    public BookAdapter(Activity activity, Book[] books) {
        this.books = new ArrayList<>(Arrays.asList(books));
        this.layoutInflater = LayoutInflater.from(activity.getApplicationContext());
        this.rootActivity = activity;
    }

    @SuppressWarnings("unused")
    public BookAdapter(Activity activity, ArrayList<Book> books) {
        this.books = books;
        this.layoutInflater = LayoutInflater.from(activity.getApplicationContext());
        this.rootActivity = activity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = layoutInflater.inflate(R.layout.item_book, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Book currentBook = books.get(position);

        holder.setData(currentBook);

        if (positionChangeListener != null)
            positionChangeListener.onPositionChange(position);

        holder.itemView.setOnClickListener(view -> {

            Intent intent = new Intent(rootActivity, BookDetailsActivity.class);
            intent.putExtra("id", currentBook.getId())
                .putExtra("name", currentBook.getName())
                .putExtra("author", currentBook.getAuthor())
                .putExtra("contributor", currentBook.getContributor().getName())
                .putExtra("publisher", currentBook.getPublisher())
                .putExtra("publication_year", currentBook.getPublicationYear())
                .putExtra("page", currentBook.getPage())
                .putExtra("type", currentBook.getType())
                .putExtra("asset_number", currentBook.getAssetNumber())
                .putExtra("registration_date", currentBook.getRegistrationDate().toString())
                .putExtra("cabinet_number", currentBook.getCabinetNumber())
                .putExtra("popularity", currentBook.getPopularity());

            //intent.putExtra("book", currentBook);

            intent.putExtra("bookJsonString", currentBook.toJson().toString());

            rootActivity.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public void removeItem(int position) {
        throw new UnsupportedOperationException("Henüz işlevsel değil.");
    }

    public void addItem(Book book) {

        books.add(book);
        notifyItemInserted(books.size() - 1);

    }

    @SuppressLint("notifyDataSetChanged")
    public void reset() {

        books.clear();
        notifyDataSetChanged();

    }

    public void setPositionChangeListener(PositionChangeListener positionChangeListener) {
        this.positionChangeListener = positionChangeListener;
    }

    public ArrayList<Book> getBooks() {
        return this.books;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tv_bookName, tv_author;
        ImageView btn_delete;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            init();

            if (CurrentUser.user.getPriority().equals(User.Priority.USER)) {
                btn_delete.setVisibility(View.GONE);

                btn_delete.setOnClickListener(view -> removeItem(getAdapterPosition()));
            }
        }

        public void setData(Book currentBook) {
            this.tv_bookName.setText(currentBook.getName());
            this.tv_author.setText(currentBook.getAuthor());

            if (currentBook.isBorrowed()) {
                tv_bookName.setTextColor(Color.RED);
            } else tv_bookName.setTextColor(Color.WHITE);
        }

        private void init() {

            tv_bookName = itemView.findViewById(R.id.item_book_bookname);
            tv_author = itemView.findViewById(R.id.item_book_author);
            btn_delete = itemView.findViewById(R.id.item_book_btnDelete);
        }
    }

    public interface PositionChangeListener {
        void onPositionChange(int position);
    }
}
