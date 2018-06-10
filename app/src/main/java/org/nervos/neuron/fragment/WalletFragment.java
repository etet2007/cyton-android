package org.nervos.neuron.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.activity.AddWalletActivity;
import org.nervos.neuron.activity.ReceiveQrCodeActivity;
import org.nervos.neuron.activity.TokenManageActivity;
import org.nervos.neuron.activity.TransferActivity;
import org.nervos.neuron.activity.WalletManageActivity;
import org.nervos.neuron.custom.TitleBar;
import org.nervos.neuron.dialog.DialogUtil;
import org.nervos.neuron.dialog.TokenTransferDialog;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.service.WalletService;
import org.nervos.neuron.util.Blockies;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.db.SharePrefUtil;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class WalletFragment extends BaseFragment {

    public static final String TAG = WalletFragment.class.getName();

    private TextView walletNameText;
    private TextView addressText;
    private FrameLayout receiveLayout;
    private FrameLayout tokenManageLayout;
    private TitleBar titleBar;
    private ImageView settingImage;
    private RecyclerView tokenRecycler;
    private CircleImageView photoImage;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TokenAdapter tokenAdapter = new TokenAdapter();

    private List<TokenItem> tokenItemList = new ArrayList<>();
    private List<String> walletNameList = new ArrayList<>();
    private WalletItem walletItem;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);
        walletNameText = view.findViewById(R.id.wallet_name);
        addressText = view.findViewById(R.id.wallet_address);
        tokenRecycler = view.findViewById(R.id.token_list);
        receiveLayout = view.findViewById(R.id.wallet_receive_layout);
        tokenManageLayout = view.findViewById(R.id.wallet_token_management_layout);
        titleBar = view.findViewById(R.id.title);
        settingImage = view.findViewById(R.id.wallet_setting);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        photoImage = view.findViewById(R.id.wallet_photo);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initWalletData(true);
        initAdapter();
        initListener();
        initTitleBarListener();
        initRefresh();
    }

    private void initWalletData(boolean showProgress) {
        if ((walletItem = DBWalletUtil.getCurrentWallet(getContext())) != null) {
            if (showProgress) showProgressBar();
            walletNameList = DBWalletUtil.getAllWalletName(getContext());
            walletNameText.setText(walletItem.name);
            addressText.setText(walletItem.address);
            photoImage.setImageBitmap(Blockies.createIcon(walletItem.address));
            WalletService.getWalletTokenBalance(getContext(), walletItem, walletItem ->
                walletNameText.post(() -> {
                    if (showProgress) dismissProgressBar();
                    swipeRefreshLayout.setRefreshing(false);
                    if (walletItem.tokenItems != null) {
                        tokenItemList = walletItem.tokenItems;
                        tokenAdapter.notifyDataSetChanged();
                    }
                })
            );
        } else {
            startActivity(new Intent(getActivity(), AddWalletActivity.class));
        }
    }

    private void initListener() {
        receiveLayout.setOnClickListener(v -> startActivity(new Intent(getActivity(), ReceiveQrCodeActivity.class)));
        tokenManageLayout.setOnClickListener(v -> startActivity(new Intent(getActivity(), TokenManageActivity.class)));
        settingImage.setOnClickListener(v -> gotoWalletManagePage());
        photoImage.setOnClickListener(v -> gotoWalletManagePage());
        addressText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("qrCode", walletItem.address);
                if (cm != null) {
                    cm.setPrimaryClip(mClipData);
                    Toast.makeText(getContext(), "复制成功", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void gotoWalletManagePage() {
        startActivity(new Intent(getActivity(), WalletManageActivity.class));
    }

    private void initRefresh() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initWalletData(false);
            }
        });
    }


    private void initAdapter() {
        tokenRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        tokenRecycler.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        tokenRecycler.setAdapter(tokenAdapter);

        tokenAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                TokenTransferDialog dialog = new TokenTransferDialog(getContext(), tokenItemList.get(position));
                dialog.setOnReceiveClickListener(v -> {
                    startActivity(new Intent(getActivity(), ReceiveQrCodeActivity.class));
                    dialog.dismiss();
                });
                dialog.setOnTransferClickListener(v -> {
                    Intent intent = new Intent(getActivity(), TransferActivity.class);
                    intent.putExtra(TransferActivity.EXTRA_TOKEN, tokenItemList.get(position));
                    startActivity(intent);
                    dialog.dismiss();
                });
                dialog.show();
            }
        });
    }

    private void initTitleBarListener() {
        titleBar.setOnRightClickListener(() -> startActivity(new Intent(getActivity(), AddWalletActivity.class)));

        titleBar.setOnLeftClickListener(() -> DialogUtil.showListDialog(getContext(), "切换当前钱包", walletNameList, which -> {
            SharePrefUtil.putCurrentWalletName(walletNameList.get(which));
            initWalletData(true);
        }));
    }


    class TokenAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public static final int VIEW_TYPE_ITEM = 1;
        public static final int VIEW_TYPE_EMPTY = 0;

        public OnItemClickListener onItemClickListener;

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            if (viewType == VIEW_TYPE_EMPTY) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_empty_view, parent, false);
                return new RecyclerView.ViewHolder(view){};
            }
            TokenViewHolder holder = new TokenViewHolder(LayoutInflater.from(
                    getActivity()).inflate(R.layout.item_token_list, parent,
                    false));
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof TokenViewHolder) {
                TokenViewHolder viewHolder = (TokenViewHolder)holder;
                viewHolder.tokenImage.setImageResource(R.drawable.ethereum);
                viewHolder.tokenName.setText(tokenItemList.get(position).symbol);
                viewHolder.tokenAmount.setText(String.valueOf(tokenItemList.get(position).balance));
                viewHolder.itemView.setTag(position);
            }
        }

        @Override
        public int getItemCount() {
            if (tokenItemList.size() == 0) {
                return 1;
            }
            return tokenItemList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (tokenItemList.size() == 0) {
                return VIEW_TYPE_EMPTY;
            }
            return VIEW_TYPE_ITEM;
        }

        class  TokenViewHolder extends RecyclerView.ViewHolder {
            SimpleDraweeView tokenImage;
            TextView tokenName;
            TextView tokenAmount;

            public TokenViewHolder (View view) {
                super(view);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onItemClick(v, (int)v.getTag());
                        }
                    }
                });
                tokenImage = view.findViewById(R.id.token_image);
                tokenName = view.findViewById(R.id.token_name);
                tokenAmount = view.findViewById(R.id.token_amount);
            }
        }
    }

    private interface OnItemClickListener{
        void onItemClick(View view, int position);
    }


}
