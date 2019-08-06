package com.amlzq.android.push;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 推送服务配置选项
 */
public class PushOptions implements Parcelable {

    public int startTime;
    public int endTime;
    public boolean sunday; // 周日
    public boolean monday;
    public boolean tuesday;
    public boolean wednesday;
    public boolean thursday;
    public boolean friday;
    public boolean saturday; // 周六

    protected PushOptions(Parcel in) {
        startTime = in.readInt();
        endTime = in.readInt();
        sunday = in.readByte() != 0;
        monday = in.readByte() != 0;
        tuesday = in.readByte() != 0;
        wednesday = in.readByte() != 0;
        thursday = in.readByte() != 0;
        friday = in.readByte() != 0;
        saturday = in.readByte() != 0;
    }

    public static final Creator<PushOptions> CREATOR = new Creator<PushOptions>() {
        @Override
        public PushOptions createFromParcel(Parcel in) {
            return new PushOptions(in);
        }

        @Override
        public PushOptions[] newArray(int size) {
            return new PushOptions[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(startTime);
        dest.writeInt(endTime);
        dest.writeByte((byte) (sunday ? 1 : 0));
        dest.writeByte((byte) (monday ? 1 : 0));
        dest.writeByte((byte) (tuesday ? 1 : 0));
        dest.writeByte((byte) (wednesday ? 1 : 0));
        dest.writeByte((byte) (thursday ? 1 : 0));
        dest.writeByte((byte) (friday ? 1 : 0));
        dest.writeByte((byte) (saturday ? 1 : 0));
    }

}
