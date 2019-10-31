package me.devsaki.hentoid.viewholders;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.viewholders.FlexibleViewHolder;
import me.devsaki.hentoid.R;
import me.devsaki.hentoid.enums.Site;

public class SiteFlex extends AbstractFlexibleItem<SiteFlex.SiteViewHolder> {

    private final Site site;
    private boolean selected = false;

    public SiteFlex(Site site) {
        this.site = site;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SiteFlex) {
            SiteFlex inItem = (SiteFlex) o;
            return this.site.equals(inItem.site);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return site.hashCode();
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_drawer_edit;
    }

    @Override
    public SiteViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
        return new SiteViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, SiteViewHolder holder, int position, List<Object> payloads) {
        holder.setContent(site, selected);
    }

    class SiteViewHolder extends FlexibleViewHolder {

        private final ImageView icon;
        private final TextView title;
        private final CheckBox chk;

        SiteViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            ImageView dragHandle = view.findViewById(R.id.drawer_item_handle);
            setDragHandleView(dragHandle);

            icon = view.findViewById(R.id.drawer_item_icon);
            title = view.findViewById(R.id.drawer_item_txt);
            chk = view.findViewById(R.id.drawer_item_chk);
        }

        void setContent(Site site, boolean checked) {
            title.setText(site.getDescription());
            icon.setImageResource(site.getIco());
            chk.setChecked(checked);
        }
    }
}
