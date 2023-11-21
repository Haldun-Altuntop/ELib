package arc.haldun.mylibrary.main.profile;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdaptor extends FragmentStateAdapter {

    public ViewPagerAdaptor(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {

            case 0:
                return new AccountFragment();

            case 1:
                return new ContributionFragment();

            case 2:
                return  new BorrowedBooksFragment();

            case 3:
                return new FriendsFragment();

            default:
                return new Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
