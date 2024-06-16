package arc.haldun.mylibrary.main.profile;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import arc.haldun.database.database.MariaDB;
import arc.haldun.database.exception.OperationFailedException;
import arc.haldun.database.objects.Book;
import arc.haldun.database.objects.CurrentUser;
import arc.haldun.mylibrary.R;
import arc.haldun.mylibrary.adapters.BookAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BorrowedBooksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BorrowedBooksFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    RecyclerView recyclerView;
    BookAdapter bookAdapter;

    public BorrowedBooksFragment() {
        // Required empty public constructor
    }

    public static BorrowedBooksFragment newInstance(String param1, String param2) {
        BorrowedBooksFragment fragment = new BorrowedBooksFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // TODO: Rename and change types of parameters
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_borrowed_books, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view); // Init views

        if (CurrentUser.user.getBorrowedBook() != 0) {

            Handler handler = new Handler(Looper.getMainLooper());

            new Thread(() -> {

                Book book = null;
                try {
                    book = new MariaDB().getBook(CurrentUser.user.getBorrowedBook());
                } catch (OperationFailedException e) {
                    throw new RuntimeException(e);
                }

                Book finalBook = book;
                handler.post(()-> {
                    bookAdapter = new BookAdapter(requireActivity(), finalBook);

                    Animation animation = AnimationUtils.loadAnimation(requireContext(), R.anim.item_animation_fall_down);
                    LayoutAnimationController animationController = new LayoutAnimationController(animation);

                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
                    linearLayoutManager.setOrientation(RecyclerView.VERTICAL);

                    recyclerView.setLayoutAnimation(animationController);
                    recyclerView.setLayoutManager(linearLayoutManager);
                    recyclerView.setAdapter(bookAdapter);
                });

            }).start();

        } else {
            Toast.makeText(requireContext(), "Ödünç alınan kitabınız yok", Toast.LENGTH_SHORT).show();
        }
    }

    private void init(View view) {
        recyclerView = view.findViewById(R.id.fragment_borrowed_books_recycler_view);
    }
}