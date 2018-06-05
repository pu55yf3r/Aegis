package me.impy.aegis.ui.views;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import me.impy.aegis.R;
import me.impy.aegis.helpers.ItemTouchHelperAdapter;

public class KeyProfileAdapter extends RecyclerView.Adapter<KeyProfileHolder> implements ItemTouchHelperAdapter {
    private ArrayList<KeyProfile> _keyProfiles;
    private static Listener _listener;
    private boolean _showIssuer;

    public KeyProfileAdapter(Listener listener) {
        _keyProfiles = new ArrayList<>();
        _listener = listener;
    }

    public void setShowIssuer(boolean showIssuer) {
        _showIssuer = showIssuer;
    }

    public void addKey(KeyProfile profile) {
        _keyProfiles.add(profile);

        int position = getItemCount() - 1;
        if (position == 0) {
            notifyDataSetChanged();
        } else {
            notifyItemInserted(position);
        }
    }

    public void removeKey(KeyProfile profile) {
        profile = getKeyByUUID(profile.getEntry().getUUID());
        int position = _keyProfiles.indexOf(profile);
        _keyProfiles.remove(position);
        notifyItemRemoved(position);
    }

    public void clearKeys() {
        _keyProfiles.clear();
        notifyDataSetChanged();
    }

    public void replaceKey(KeyProfile newProfile) {
        KeyProfile oldProfile = getKeyByUUID(newProfile.getEntry().getUUID());
        int position = _keyProfiles.indexOf(oldProfile);
        _keyProfiles.set(position, newProfile);
        notifyItemChanged(position);
    }

    private KeyProfile getKeyByUUID(UUID uuid) {
        for (KeyProfile profile : _keyProfiles) {
            if (profile.getEntry().getUUID().equals(uuid)) {
                return profile;
            }
        }
        throw new AssertionError("no key profile found with the same id");
    }

    @Override
    public void onItemDismiss(int position) {

    }

    @Override
    public void onItemDrop(int position) {
        _listener.onKeyProfileDrop(_keyProfiles.get(position));
    }

    @Override
    public void onItemMove(int firstPosition, int secondPosition) {
        // notify the database first
        _listener.onKeyProfileMove(_keyProfiles.get(firstPosition), _keyProfiles.get(secondPosition));

        // update our side of things
        Collections.swap(_keyProfiles, firstPosition, secondPosition);
        notifyItemMoved(firstPosition, secondPosition);
    }

    @Override
    public KeyProfileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_keyprofile, parent, false);
        return new KeyProfileHolder(view);
    }

    @Override
    public void onViewRecycled(KeyProfileHolder holder) {
        holder.stopRefreshLoop();
        super.onViewRecycled(holder);
    }

    @Override
    public void onBindViewHolder(final KeyProfileHolder holder, int position) {
        boolean uniform = isPeriodUniform();
        final KeyProfile profile = _keyProfiles.get(position);
        holder.setData(profile, _showIssuer, !uniform);
        if (!uniform) {
            holder.startRefreshLoop();
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                _listener.onKeyProfileClick(_keyProfiles.get(position));
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = holder.getAdapterPosition();
                return _listener.onLongKeyProfileClick(_keyProfiles.get(position));
            }
        });
    }

    public int getUniformPeriod() {
        if (_keyProfiles.isEmpty()) {
            return -1;
        }

        int period = _keyProfiles.get(0).getEntry().getInfo().getPeriod();
        for (KeyProfile profile : _keyProfiles) {
            if (period != profile.getEntry().getInfo().getPeriod()) {
                return -1;
            }
        }

        return period;
    }

    public boolean isPeriodUniform() {
        return getUniformPeriod() != -1;
    }

    @Override
    public int getItemCount() {
        return _keyProfiles.size();
    }

    public interface Listener {
        void onKeyProfileClick(KeyProfile profile);
        boolean onLongKeyProfileClick(KeyProfile profile);
        void onKeyProfileMove(KeyProfile profile1, KeyProfile profile2);
        void onKeyProfileDrop(KeyProfile profile);
    }
}
