package ali.abdullahmansour.egypt.CompanyApp.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ali.abdullahmansour.egypt.CompaniesFragment;
import ali.abdullahmansour.egypt.R;
import ali.abdullahmansour.egypt.SignInFragment;
import ali.abdullahmansour.egypt.SignUpFragment;
import ali.abdullahmansour.egypt.TouristsFragment;

public class TourGuideFragment extends Fragment
{
    View view;
    FragmentPagerAdapter fragmentPagerAdapter;
    ViewPager viewPager;
    TabLayout tabLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {

        view = inflater.inflate(R.layout.tour_guide_fragment, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        viewPager = view.findViewById(R.id.viewpager);
        tabLayout = view.findViewById(R.id.tabs);

        fragmentPagerAdapter = new FragmentPagerAdapter(getChildFragmentManager())
        {
            private final Fragment[] mFragments = new Fragment[]
                    {
                            new CompaniesFragment(),
                            new TouristsFragment()
                    };
            private final String[] mFragmentNames = new String[]
                    {
                            "COMPANIES",
                            "TOURISTS",
                    };

            @Override
            public Fragment getItem(int position)
            {
                return mFragments[position];
            }

            @Override
            public int getCount()
            {
                return mFragments.length;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position)
            {
                return mFragmentNames[position];
            }
        };

        viewPager.setAdapter(fragmentPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }
}
