package arc.haldun.hurda.mobile.parameters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayoutManager;

import java.util.ArrayList;
import java.util.List;

import arc.haldun.hurda.database.objects.GeneralParameter;
import arc.haldun.hurda.mobile.R;
import arc.haldun.hurda.mobile.Utilities;

public class GeneralParameterAdapter extends RecyclerView.Adapter<GeneralParameterAdapter.ViewHolder> {

    private final List<GeneralParameter> generalParameters;
    private OnItemClickedListener onItemClicked;
    private OnItemLongClickedListener onItemLongClicked;

    public GeneralParameterAdapter(GeneralParameter[] generalParameters) {
        this.generalParameters = new ArrayList<>(List.of(generalParameters));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_general_parameter, parent, false);

        FlexboxLayoutManager.LayoutParams params = new FlexboxLayoutManager.LayoutParams(
                (Utilities.SCREEN_WIDTH - 30) / 3,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(5,5,5,5);
        view.setLayoutParams(params);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.setData(generalParameters.get(
                position).getName(),
                generalParameters.get(position).getValue()
        );

        holder.itemView.setOnClickListener(v -> {
            if (onItemClicked != null) {
                onItemClicked.onItemClicked(generalParameters.get(position));
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onItemClicked != null) {
                onItemLongClicked.onItemLongClicked(generalParameters.get(position));
            }
            return false;
        });
    }

    public void removeParameter(GeneralParameter parameter) {
        int index = positionOf(parameter);
        generalParameters.remove(index);
        notifyItemRemoved(index);
    }

    public void updateParameter(GeneralParameter parameter) {
        int index = positionOf(parameter);
        generalParameters.set(index, parameter);
        notifyItemChanged(index);
    }

    public int positionOf(GeneralParameter parameter) {
        for (int i = 0; i < generalParameters.size(); i++) {
            if (generalParameters.get(i).getName().equals(parameter.getName())) {
                return i;
            }
        }

        throw new RuntimeException("Parametre bulunamadı:" + parameter.getName());
    }

    @Override
    public int getItemCount() {
        return generalParameters.size();
    }

    public void addParameter(GeneralParameter parameter) {
        generalParameters.add(parameter);
        notifyItemInserted(generalParameters.size() - 1);
    }

    public void setOnItemClicked(OnItemClickedListener onItemClicked) {
        this.onItemClicked = onItemClicked;
    }

    public void setOnItemLongClicked(OnItemLongClickedListener onItemLongClicked) {
        this.onItemLongClicked = onItemLongClicked;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvParameterName, tvParameterValue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvParameterName = itemView.findViewById(R.id.item_general_parameter_tv_parameter_name);
            tvParameterValue = itemView.findViewById(R.id.item_general_parameter_tv_parameter_value);
        }

        public void setData(String parameterName, double parameterValue) {
            this.tvParameterName.setText(parameterName);
            this.tvParameterValue.setText(String.valueOf(parameterValue));
        }
    }
}
