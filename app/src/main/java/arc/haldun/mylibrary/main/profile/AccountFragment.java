package arc.haldun.mylibrary.main.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import arc.haldun.database.objects.CurrentUser;
import arc.haldun.mylibrary.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment implements View.OnClickListener {

    TextView tv_username, tv_email, tv_password;
    EditText et_username, et_email, et_password;
    Button btn_save;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AccountFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AccountFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AccountFragment newInstance(String param1, String param2) {
        AccountFragment fragment = new AccountFragment();
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
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view); // Declare views

        //
        // Init
        //
        tv_username.setText(getString(R.string.user_name));
        tv_email.setText(getString(R.string.prompt_email));
        tv_password.setText(getString(R.string.password));

        et_username.setText(CurrentUser.user.getName());
        et_email.setText(CurrentUser.user.getEMail());
        et_password.setText(CurrentUser.user.getPassword());

        btn_save.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if (view.equals(btn_save)) {

            saveChanges();
        }
    }

    private void saveChanges() {

        // TODO: Improve this code

        Toast.makeText(requireContext(), getString(R.string.not_supported_yet), Toast.LENGTH_SHORT).show();

    }

    private void init(View view) {

        tv_username = view.findViewById(R.id.account_fragment_tv_username);
        tv_email = view.findViewById(R.id.account_fragment_tv_email);
        tv_password = view.findViewById(R.id.account_fragment_tv_password);

        et_username = view.findViewById(R.id.account_fragment_et_username);
        et_email = view.findViewById(R.id.account_fragment_et_email);
        et_password = view.findViewById(R.id.account_fragment_et_password);

        btn_save = view.findViewById(R.id.account_fragment_btn_save);
    }
}