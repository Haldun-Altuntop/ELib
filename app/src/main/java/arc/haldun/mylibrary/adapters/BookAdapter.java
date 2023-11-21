package arc.haldun.mylibrary.adapters;

import  android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;

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

    public BookAdapter(Activity activity, Book[] books) {
        this.books = new ArrayList<>(Arrays.asList(books));
        this.layoutInflater = LayoutInflater.from(activity.getApplicationContext());
        this.rootActivity = activity;
    }

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

        positionChangeListener.onPositionChange(position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(rootActivity, BookDetailsActivity.class);
                intent.putExtra("id", currentBook.getId());
                intent.putExtra("name", currentBook.getName());
                intent.putExtra("author", currentBook.getAuthor());
                intent.putExtra("owner", currentBook.getContributor().getName());

                /*
                User borrower;
                if ((borrower = currentBook.borrowedBy()) != null) {
                    intent.putExtra("borrower", borrower.getName());
                } else {
                    intent.putExtra("borrower", "null");
                }*/



                rootActivity.startActivity(intent);
            }
        });

        /*
        Animation animation = AnimationUtils.loadAnimation(rootActivity.getApplicationContext(), R.anim.item_animation_fall_down);
        holder.itemView.startAnimation(animation);
         */
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public void removeItem(int position) {


    }

    public void addItem(Book book) {

        books.add(book);
        notifyItemInserted(books.size() - 1);

    }

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

            if (CurrentUser.user.getPriority().equals(User.USER)) {
                btn_delete.setVisibility(View.GONE);

                btn_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeItem(getAdapterPosition());
                    }
                });
            }
        }

        public void setData(Book currentBook) {
            this.tv_bookName.setText(currentBook.getName());
            this.tv_author.setText(currentBook.getAuthor());
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
