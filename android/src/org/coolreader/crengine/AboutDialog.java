package org.coolreader.crengine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import org.coolreader.CoolReader;
import org.coolreader.CoolReader.DonationListener;
import org.coolreader.R;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;

public class AboutDialog extends BaseDialog implements TabContentFactory {
	final CoolReader mCoolReader;
	
	private View mAppTab;
	private View mDirsTab;
	private View mLicenseTab;
	private View mDonationTab;

	private boolean isPackageInstalled( String packageName ) {
		try {
			mCoolReader.getPackageManager().getApplicationInfo(packageName, 0);
			return true;
		} catch ( Exception e ) {
			return false;
		}
	}

	private void installPackage( String packageName ) {
		Log.i("cr3", "installPackageL " + packageName);
		try {
			mCoolReader.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
		} catch ( ActivityNotFoundException e ) {
			mCoolReader.showToast("Cannot run Android Market application");
		}
	}
	
	private void setupDonationButton( final Button btn, final String packageName ) {
		if ( isPackageInstalled(packageName)) {
			btn.setEnabled(false);
			btn.setText(R.string.dlg_about_donation_installed);
		} else {
			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					installPackage(packageName);
				}
			});
		}
	}
	
	private void setupInAppDonationButton( final Button btn, final double amount ) {
		btn.setText("$" + amount);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCoolReader.makeDonation(amount);
			}
		});
	}
	
	private void updateTotalDonations() {
		double amount = mCoolReader.getTotalDonations();
		if (isPackageInstalled("org.coolreader.donation.gold"))
			amount += 10.0;
		if (isPackageInstalled("org.coolreader.donation.silver"))
			amount += 3.0;
		if (isPackageInstalled("org.coolreader.donation.bronze"))
			amount += 1.0;
		TextView text = ((TextView)mDonationTab.findViewById(R.id.btn_about_donation_total));
		if (text != null)
			text.setText(mCoolReader.getString(R.string.dlg_about_donation_total) + " $" + amount);
	}

	public AboutDialog( CoolReader activity)
	{
		super(activity);
		mCoolReader = activity;
		boolean isFork = !mCoolReader.getPackageName().equals(CoolReader.class.getPackage().getName());
		setTitle(R.string.dlg_about);
		LayoutInflater inflater = LayoutInflater.from(getContext());
		final TabHost tabs = (TabHost)inflater.inflate(R.layout.about_dialog, null);

		TypedArray a = activity.getTheme().obtainStyledAttributes(new int[]
				{R.attr.colorThemeGray2, R.attr.colorThemeGray2Contrast});
		final int colorGray = a.getColor(0, Color.GRAY);
		final int colorGrayC = a.getColor(1, Color.GRAY);

		tabs.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				for (int i = 0; i < tabs.getTabWidget().getChildCount(); i++) {
					tabs.getTabWidget().getChildAt(i)
							.setBackgroundColor(colorGrayC); // unselected
				}

				tabs.getTabWidget().getChildAt(tabs.getCurrentTab())
						.setBackgroundColor(colorGray); // selected

			}
		});

		mAppTab = (View)inflater.inflate(R.layout.about_dialog_app, null);
		if (isFork) {
			((TextView) mAppTab.findViewById(R.id.version)).setText("Cool Reader (plotn mod) " + mCoolReader.getVersion());
			((TextView) mAppTab.findViewById(R.id.www1)).setText("https://github.com/plotn/coolreader");
		} else {
			((TextView) mAppTab.findViewById(R.id.version)).setText("Cool Reader " + mCoolReader.getVersion());
		}

		mDirsTab = (View)inflater.inflate(R.layout.about_dialog_dirs, null);
		TextView fonts_dir = (TextView)mDirsTab.findViewById(R.id.fonts_dirs);

		ArrayList<String> fontsDirs = Engine.getFontsDirs();
		StringBuilder sbuf = new StringBuilder();
		Iterator<String> it = fontsDirs.iterator();
		while (it.hasNext()) {
			String s = it.next();
			sbuf.append(s);
			if (it.hasNext()) {
				sbuf.append("\n");
			}
		}
		fonts_dir.setText(sbuf.toString());

		ArrayList<String> testuresDirs = Engine.getDataDirs(Engine.DataDirType.TexturesDirs);
		sbuf = new StringBuilder();
		it = testuresDirs.iterator();
		while (it.hasNext()) {
			String s = it.next();
			sbuf.append(s);
			if (it.hasNext()) {
				sbuf.append("\n");
			}
		}
		TextView textures_dir = (TextView)mDirsTab.findViewById(R.id.textures_dirs);
		textures_dir.setText(sbuf.toString());

		ArrayList<String> backgroundsDirs = Engine.getDataDirs(Engine.DataDirType.BackgroundsDirs);
		sbuf = new StringBuilder();
		it = backgroundsDirs.iterator();
		while (it.hasNext()) {
			String s = it.next();
			sbuf.append(s);
			if (it.hasNext()) {
				sbuf.append("\n");
			}
		}
		TextView backgrounds_dir = (TextView)mDirsTab.findViewById(R.id.backgrounds_dirs);
		backgrounds_dir.setText(sbuf.toString());

		ArrayList<String> hyphDirs = Engine.getDataDirs(Engine.DataDirType.HyphsDirs);
		sbuf = new StringBuilder();
		it = hyphDirs.iterator();
		while (it.hasNext()) {
			String s = it.next();
			sbuf.append(s);
			if (it.hasNext()) {
				sbuf.append("\n");
			}
		}
		TextView hyph_dir = (TextView)mDirsTab.findViewById(R.id.hyph_dirs);
		hyph_dir.setText(sbuf.toString());

		mLicenseTab = (View)inflater.inflate(R.layout.about_dialog_license, null);
		String license = Engine.getInstance(mCoolReader).loadResourceUtf8(R.raw.license);
		((TextView)mLicenseTab.findViewById(R.id.license)).setText(license);
        boolean billingSupported = mCoolReader.isDonationSupported() && !isFork;
		mDonationTab = (View)inflater.inflate(billingSupported ? R.layout.about_dialog_donation2 : R.layout.about_dialog_donation, null);

		if (billingSupported) {
			setupInAppDonationButton( (Button)mDonationTab.findViewById(R.id.btn_about_donation_install_vip), 100);
			setupInAppDonationButton( (Button)mDonationTab.findViewById(R.id.btn_about_donation_install_platinum), 30);
			setupInAppDonationButton( (Button)mDonationTab.findViewById(R.id.btn_about_donation_install_gold), 10);
			setupInAppDonationButton( (Button)mDonationTab.findViewById(R.id.btn_about_donation_install_silver), 3);
			setupInAppDonationButton( (Button)mDonationTab.findViewById(R.id.btn_about_donation_install_bronze), 1);
			setupInAppDonationButton( (Button)mDonationTab.findViewById(R.id.btn_about_donation_install_iron), 0.3);
			updateTotalDonations();
			mCoolReader.setDonationListener(new DonationListener() {
				@Override
		    	public void onDonationTotalChanged(double total) {
		    		updateTotalDonations();
		    	}
		    });
			setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					mCoolReader.setDonationListener(null);
				}
			});
		} else {
			setupDonationButton( (Button)mDonationTab.findViewById(R.id.btn_about_donation_install_gold), "org.coolreader.donation.gold");
			setupDonationButton( (Button)mDonationTab.findViewById(R.id.btn_about_donation_install_silver), "org.coolreader.donation.silver");
			setupDonationButton( (Button)mDonationTab.findViewById(R.id.btn_about_donation_install_bronze), "org.coolreader.donation.bronze");
		}
		
		tabs.setup();
		TabHost.TabSpec tsApp = tabs.newTabSpec("App");
		tsApp.setIndicator("",
				getContext().getResources().getDrawable(Utils.resolveResourceIdByAttr(activity, R.attr.attr_icons8_link, R.drawable.icons8_link)));
				//getContext().getResources().getDrawable(R.drawable.icons8_link));
		tsApp.setContent(this);
		tabs.addTab(tsApp);

		TabHost.TabSpec tsDirectories = tabs.newTabSpec("Directories");
		tsDirectories.setIndicator("",
				getContext().getResources().getDrawable(Utils.resolveResourceIdByAttr(activity, R.attr.attr_icons8_folder_2, R.drawable.icons8_folder_2)));
				//getContext().getResources().getDrawable(R.drawable.icons8_folder_2));
		tsDirectories.setContent(this);
		tabs.addTab(tsDirectories);

		TabHost.TabSpec tsLicense = tabs.newTabSpec("License");
		tsLicense.setIndicator("",
				getContext().getResources().getDrawable(Utils.resolveResourceIdByAttr(activity, R.attr.attr_icons8_star, R.drawable.icons8_star)));
				//getContext().getResources().getDrawable(R.drawable.icons8_star));
		tsLicense.setContent(this);
		tabs.addTab(tsLicense);
		
		TabHost.TabSpec tsDonation = tabs.newTabSpec("Donation");
		tsDonation.setIndicator("",
				getContext().getResources().getDrawable(Utils.resolveResourceIdByAttr(activity, R.attr.attr_icons8_happy, R.drawable.icons8_happy)));
				//getContext().getResources().getDrawable(R.drawable.icons8_happy));
		tsDonation.setContent(this);
		tabs.addTab(tsDonation);
		
		setView( tabs );
		tabs.setCurrentTab(1);
		tabs.setCurrentTab(0);
		// 25% chance to show Donations tab
		if ((rnd.nextInt() & 3) == 3)
			tabs.setCurrentTab(3);
		
	}
	private static Random rnd = new Random(android.os.SystemClock.uptimeMillis()); 

	
	@Override
	public View createTabContent(String tag) {

		if ( "App".equals(tag) )
			return mAppTab;
		else if ( "Directories".equals(tag) )
			return mDirsTab;
		else if ( "License".equals(tag) )
			return mLicenseTab;
		else if ( "Donation".equals(tag) )
			return mDonationTab;
		return null;
	}
	
}
