package fr.mathis.tourhanoipro.ui.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import fr.mathis.tourhanoipro.R;
import fr.mathis.tourhanoipro.core.tools.Tools;
import fr.mathis.tourhanoipro.view.game.GameView;

public class DiskColorPickerAdpater extends RecyclerView.Adapter {
    LayoutInflater inflater;
    Context context;
    SettingsFragment.IDiskColorSelected clickListener;

    public DiskColorPickerAdpater(LayoutInflater _inflater, Context _context, SettingsFragment.IDiskColorSelected _clickListener) {
        inflater = _inflater;
        context = _context;
        clickListener = _clickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DiskColorViewHolder(inflater.inflate(R.layout.template_game_disk_color_picker, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        DiskColorViewHolder holderT = ((DiskColorViewHolder) holder);

        holderT.gv.setDisabled(true);
        holderT.gv.setDemoMode(GameView.MODE_COLOR_PICKER);
        int[] colors = Tools.getDiskColors(context, position);
        holderT.gv.setColorPalette(colors);
        holderT.gv.createNewGame(6);

        holderT.tv.setText(Tools.getColorName(position));
    }

    @Override
    public int getItemCount() {
        return Tools.DISK_COLOR_COUNT;
    }

    class DiskColorViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tv;
        GameView gv;

        public DiskColorViewHolder(View itemView) {
            super(itemView);

            tv = itemView.findViewById(R.id.tvColorDiskTitle);
            gv = itemView.findViewById(R.id.gvColorDisk);

            itemView.findViewById(R.id.vClickableArea).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.selected(getLayoutPosition());
        }
    }
}


