package arc.haldun.hurda.mobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import arc.haldun.hurda.database.objects.Scrap;

public class ScrapAdapter extends RecyclerView.Adapter<ScrapAdapter.ScrapViewHolder> {

    private final LayoutInflater layoutInflater;
    private List<Scrap> scraps;

    public ScrapAdapter(Context context, List<Scrap> scraps) {
        layoutInflater = LayoutInflater.from(context);
        this.scraps = scraps;

        for (Scrap scrap : scraps) {
            ScrapHolder scrapHolder = new ScrapHolder();
            scrapHolder.setScrap(scrap);
            ScrapHolderManager.addScrapHolder(scrapHolder);
        }
    }

    @NonNull
    @Override
    public ScrapViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_scrap, parent, false);
        return new ScrapViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScrapViewHolder holder, int position) {
        if (position >= 0 && position < ScrapHolderManager.getSize()) {
            holder.setData(ScrapHolderManager.getScrapHolder(position).getScrap());
        }
        ScrapHolderManager.getScrapHolder(position).setPercentage(holder.getPercentage());
    }

    @Override
    public int getItemCount() {
        return ScrapHolderManager.getSize();
    }

    static class ScrapViewHolder extends RecyclerView.ViewHolder {

        private TextView tvScrapName;
        private SeekBar seekBar;
        private TextView tvPercentage;

        private int lastPercentage = 0;

        public ScrapViewHolder(@NonNull View itemView) {
            super(itemView);
            init(itemView);

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    if (fromUser) {

                        if (ScrapHolderManager.totalPercentage >= 100) {

                            if (getLastPercentage() < progress) {
                                seekBar.setProgress(lastPercentage);
                                return;
                            }
                        }

                        ScrapHolderManager.getScrapHolder(getAdapterPosition()).setPercentage(progress);
                        ScrapHolderManager.calculateEnergy();

                        setLastPercentage(progress);

                        int fazlalık = ScrapHolderManager.totalPercentage - 100;
                        if (fazlalık > 0) {
                            seekBar.setProgress(progress - fazlalık);
                            setLastPercentage(progress - fazlalık);

                            ScrapHolderManager.getScrapHolder(getAdapterPosition()).setPercentage(progress - fazlalık);
                            ScrapHolderManager.calculateEnergy();
                        }
                    }

                    tvPercentage.setText("%" + seekBar.getProgress());
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            itemView.setOnClickListener(this::onClick);
        }

        private void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle(tvScrapName.getText().toString())
                    .setMessage(ScrapHolderManager.getScrapHolder(getAdapterPosition()).getScrap().toString())
                    .setPositiveButton("kapat", null);

            AlertDialog dialog = builder.create();
            dialog.show();
        }

        public void setData(Scrap scrap) {
            tvScrapName.setText(scrap.getP01_name());
        }

        public int getLastPercentage() {
            return lastPercentage;
        }

        public void setLastPercentage(int lastPercentage) {
            this.lastPercentage = lastPercentage;
        }

        public int getPercentage() {
            return seekBar.getProgress();
        }

        private void init(View v) {
            tvScrapName = v.findViewById(R.id.item_scrap_tv_scrap_name);
            seekBar = v.findViewById(R.id.item_scrap_seek_bar);
            tvPercentage = v.findViewById(R.id.item_scrap_tv_percentage);

            tvPercentage.setText("%0");
        }
    }
}
