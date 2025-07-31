package arc.haldun.hurda.mobile;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import arc.haldun.hurda.database.objects.Scrap;

public class ScrapAdapter extends RecyclerView.Adapter<ScrapAdapter.ScrapViewHolder> {

    private View.OnFocusChangeListener onFocusChangeListener;

    public ScrapAdapter(List<Scrap> scraps) {

        for (Scrap scrap : scraps) {
            ScrapHolder scrapHolder = new ScrapHolder();
            scrapHolder.setScrap(scrap);
            ScrapHolderManager.addScrapHolder(scrapHolder);
        }
    }

    @NonNull
    @Override
    public ScrapViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scrap, parent, false);
        return new ScrapViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScrapViewHolder holder, int position) {
        if (position >= 0 && position < ScrapHolderManager.getSize()) {
            holder.setData(ScrapHolderManager.getScrapHolder(position).getScrap());
            holder.tvPercentage.setOnFocusChangeListener(this.onFocusChangeListener);
        }
        ScrapHolderManager.getScrapHolder(position).setPercentage(holder.getPercentage());
    }

    @Override
    public int getItemCount() {
        return ScrapHolderManager.getSize();
    }

    public void setOnItemFocusChangeListener(View.OnFocusChangeListener onFocusChangeListener) {
        this.onFocusChangeListener = onFocusChangeListener;
    }

    public static class ScrapViewHolder extends RecyclerView.ViewHolder {

        private TextView tvScrapName;
        private SeekBar seekBar;
        private EditText tvPercentage;

        private AlertDialog dialog;
        private View scrapDetailsPane;
        private EditText etScrapName, etPrice, etStock, etC, etSi, etMn, etP, etS, etFe, etO,
                etH2O, etCaO, etMgO, etAl2O3, etSiO2, etCu, etNi, etCr, etSn, etMo, etSlag, etYield,
                etDH, etMeltingFactor;

        private double lastPercentage = 0;

        public ScrapViewHolder(@NonNull View itemView) {
            super(itemView);
            init(itemView);

            seekBar.setMax(1000);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    if (fromUser) {

                        setPercentage(progress);

                        double p = (double) seekBar.getProgress() / 10;

                        tvPercentage.setText(itemView.getContext().getString(R.string.percentage, p));

                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            itemView.setOnClickListener(this::onClick);

            tvPercentage.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.toString().isEmpty()) return;
                    String str = s.toString().replace(",", ".");
                    float input = Float.parseFloat(str);

                    int progress = (int) (input * 10);

                    setPercentage(progress);

                    seekBar.setProgress(progress);
                }
            });
        }

        private void setPercentage(int progress) {
            if (ScrapHolderManager.totalPercentage >= 100) {

                if (getLastPercentage() < (double) progress / 10) {
                    seekBar.setProgress((int) lastPercentage * 10, true);
                    return;
                }
            }

            ScrapHolderManager.getScrapHolder(getAdapterPosition()).setPercentage((double) progress / 10);
            ScrapHolderManager.calculateTotalPercentage();
            //ScrapHolderManager.calculateEnergy();

            setLastPercentage(progress / 10);

            int fazlalik = (int) (ScrapHolderManager.totalPercentage * 10 - 1000);
            if (fazlalik > 0) {
                seekBar.setProgress(progress - fazlalik);
                setLastPercentage(progress - fazlalik);

                ScrapHolderManager.getScrapHolder(getAdapterPosition()).setPercentage((double) (progress - fazlalik) / 10);
                //ScrapHolderManager.calculateEnergy();
            }
        }

        private void onClick(View v) {

            Scrap currentScrap = ScrapHolderManager.getScrapHolder(getAdapterPosition()).getScrap();
            etScrapName.setText(currentScrap.getP01_name());
            etPrice.setText(String.valueOf(currentScrap.getP02_price()));
            etStock.setText(String.valueOf(currentScrap.getP03_stock()));
            etC.setText(String.valueOf(currentScrap.getP04_C()));
            etSi.setText(String.valueOf(currentScrap.getP05_Si()));
            etMn.setText(String.valueOf(currentScrap.getP06_Mn()));
            etP.setText(String.valueOf(currentScrap.getP07_P()));
            etS.setText(String.valueOf(currentScrap.getP08_S()));
            etFe.setText(String.valueOf(currentScrap.getP09_Fe()));
            etO.setText(String.valueOf(currentScrap.getP10_O()));
            etH2O.setText(String.valueOf(currentScrap.getP11_H2O()));
            etCaO.setText(String.valueOf(currentScrap.getP12_CaO()));
            etMgO.setText(String.valueOf(currentScrap.getP13_MgO()));
            etAl2O3.setText(String.valueOf(currentScrap.getP14_Al2O3()));
            etSiO2.setText(String.valueOf(currentScrap.getP15_SiO2()));
            etCu.setText(String.valueOf(currentScrap.getP16_Cu()));
            etNi.setText(String.valueOf(currentScrap.getP17_Ni()));
            etCr.setText(String.valueOf(currentScrap.getP18_Cr()));
            etSn.setText(String.valueOf(currentScrap.getP19_Sn()));
            etMo.setText(String.valueOf(currentScrap.getP20_Mo()));
            //etSlag.setText(String.valueOf(currentScrap.getP21_slag()));
            //etYield.setText(String.valueOf(currentScrap.getP22_yield()));
            //etDH.setText(String.valueOf(currentScrap.getP23_dH()));
            etMeltingFactor.setText(String.valueOf(currentScrap.getP24_meltingFactor()));

            dialog.show();
        }

        public void setData(Scrap scrap) {
            tvScrapName.setText(scrap.getP01_name());
        }

        public double getLastPercentage() {
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
            tvPercentage.setText("0");

            scrapDetailsPane = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_scrap_details, null);
            etScrapName = scrapDetailsPane.findViewById(R.id.dialog_scrap_details_et_scrap_name);
            etPrice = scrapDetailsPane.findViewById(R.id.dialog_scrap_details_et_price);
            etStock = scrapDetailsPane.findViewById(R.id.dialog_scrap_details_et_stock);
            etC = scrapDetailsPane.findViewById(R.id.dialog_scrap_details_et_C);
            etSi = scrapDetailsPane.findViewById(R.id.dialog_scrap_details_et_Si);
            etMn = scrapDetailsPane.findViewById(R.id.dialog_scrap_details_et_Mn);
            etP = scrapDetailsPane.findViewById(R.id.dialog_scrap_details_et_P);
            etS = scrapDetailsPane.findViewById(R.id.dialog_scrap_details_et_S);
            etFe = scrapDetailsPane.findViewById(R.id.dialog_scrap_details_et_Fe);
            etO = scrapDetailsPane.findViewById(R.id.dialog_scrap_details_et_O);
            etH2O = scrapDetailsPane.findViewById(R.id.dialog_scrap_details_et_H2O);
            etCaO = scrapDetailsPane.findViewById(R.id.dialog_scrap_details_et_CaO);
            etMgO = scrapDetailsPane.findViewById(R.id.dialog_scrap_details_et_MgO);
            etAl2O3 = scrapDetailsPane.findViewById(R.id.dialog_scrap_details_et_Al2O3);
            etSiO2 = scrapDetailsPane.findViewById(R.id.dialog_scrap_details_et_SiO2);
            etCu = scrapDetailsPane.findViewById(R.id.dialog_scrap_details_et_Cu);
            etNi = scrapDetailsPane.findViewById(R.id.dialog_scrap_details_et_Ni);
            etCr = scrapDetailsPane.findViewById(R.id.dialog_scrap_details_et_Cr);
            etSn = scrapDetailsPane.findViewById(R.id.dialog_scrap_details_et_Sn);
            etMo = scrapDetailsPane.findViewById(R.id.dialog_scrap_details_et_Mo);
            etSlag = scrapDetailsPane.findViewById(R.id.dialog_scrap_details_et_slag);
            etYield = scrapDetailsPane.findViewById(R.id.dialog_scrap_details_et_yield);
            etDH = scrapDetailsPane.findViewById(R.id.dialog_scrap_details_et_dH);
            etMeltingFactor = scrapDetailsPane.findViewById(R.id.dialog_scrap_details_et_melting_factor);

            dialog = createScrapDetailsDialog();

        }

        private AlertDialog createScrapDetailsDialog() {

            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
            builder.setTitle(tvScrapName.getText().toString())
                    //.setMessage(ScrapHolderManager.getScrapHolder(getAdapterPosition()).getScrap().toString())
                    .setView(scrapDetailsPane)
                    .setPositiveButton("kapat", null);


            return builder.create();
        }
    }
}
