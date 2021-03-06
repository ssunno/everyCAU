package univ.cau.ssunno.everycau.meals;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import univ.cau.ssunno.everycau.R;
import univ.cau.ssunno.everycau.utils.network.MenuInfo;

/*
* Meals card 하나에서 내부의 식단을 관리하는 어댑터
* */
public class DishAdapter extends RecyclerView.Adapter<DishAdapter.DishViewHolder> {
    private ArrayList<MenuInfo> menuInfos;
    @Override
    public int getItemCount() {
        return menuInfos.size();
    }

    public DishAdapter() {
        this.menuInfos = new ArrayList<>();
    }

    public void setMenuInfos(ArrayList<MenuInfo> menuInfos){
        while (!this.menuInfos.isEmpty()) this.menuInfos.remove(0);
        for ( MenuInfo mi : menuInfos) this.menuInfos.add(mi);
    }

    @Override
    public void onBindViewHolder(DishViewHolder holder, int position) {
        holder.dishType.setText(this.menuInfos.get(position).getStyle());
        holder.dishPrice.setText(this.menuInfos.get(position).getPrice());
        while (!holder.dishs.isEmpty()) holder.dishs.remove(0);
        for (String menu : this.menuInfos.get(position).getDishs()) holder.dishs.add(menu);
        // setListViewHeightBasedOnItems(holder.dishList);
        holder.dishsAdapter.notifyDataSetChanged();
    }

    @Override
    public DishViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.meals_card_menu_dishlist, parent, false);
        return new DishViewHolder(view);
    }

    public static class DishViewHolder extends RecyclerView.ViewHolder {
        public TextView dishType, dishPrice;
        public ImageView verticalLine;
        public ListView dishList;
        public ArrayAdapter<String> dishsAdapter;
        public ArrayList<String> dishs;
        public DishViewHolder(View itemView) {
            super(itemView);
            dishType = (TextView)itemView.findViewById(R.id.meals_card_dish_style);
            dishPrice = (TextView)itemView.findViewById(R.id.meals_card_dish_price);
            verticalLine = (ImageView)itemView.findViewById(R.id.meals_card_dish_verticalline);
            dishList = (ListView) itemView.findViewById(R.id.meals_card_dish_listview);
            dishs = new ArrayList<>();
            dishsAdapter = new ArrayAdapter<>(itemView.getContext(), R.layout.simple_list_item, dishs);
            dishList.setAdapter(dishsAdapter);
            dishList.setDivider(null);
        }
    }
}