package fr.neamar.lolgamedata.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import fr.neamar.lolgamedata.R;
import fr.neamar.lolgamedata.SnackBarActivity;
import fr.neamar.lolgamedata.holder.AccountHolder;
import fr.neamar.lolgamedata.pojo.Account;

/**
 * Created by neamar on 25/03/16.
 */
public class AccountAdapter extends RecyclerView.Adapter<AccountHolder> {
    public ArrayList<Account> accounts;
    public SnackBarActivity snackBarActivity;

    public AccountAdapter(ArrayList<Account> accounts, SnackBarActivity snackBarActivity) {
        this.accounts = accounts;
        this.snackBarActivity = snackBarActivity;
        setHasStableIds(true);
    }

    @Override
    public AccountHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view = inflater.inflate(R.layout.item_account, parent, false);

        return new AccountHolder(view);
    }

    public void updateAccounts(ArrayList<Account> accounts)  {
        this.accounts = accounts;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(AccountHolder holder, int position) {
        holder.bindAdvert(this, accounts.get(position));
    }

    @Override
    public long getItemId(int position) {
        return accounts.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }
}