package org.dev.hehe.dto.kakao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoAddressResponse {

    @JsonProperty("documents")
    private List<Document> documents;

    @JsonProperty("meta")
    private Meta meta;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Document {

        @JsonProperty("address_name")
        private String addressName;

        @JsonProperty("address_type")
        private String addressType; // ROAD_ADDR, REGION_ADDR, USER_DEFINED

        @JsonProperty("address")
        private Address address;

        @JsonProperty("road_address")
        private RoadAddress roadAddress;

        @JsonProperty("x")
        private String x; // 경도 (Longitude)

        @JsonProperty("y")
        private String y; // 위도 (Latitude)

        public double getLongitude() {
            return Double.parseDouble(x);
        }

        public double getLatitude() {
            return Double.parseDouble(y);
        }
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {

        @JsonProperty("address_name")
        private String addressName;

        @JsonProperty("region_1depth_name")
        private String region1depthName;

        @JsonProperty("region_2depth_name")
        private String region2depthName;

        @JsonProperty("region_3depth_name")
        private String region3depthName;

        @JsonProperty("region_3depth_h_name")
        private String region3depthHName;

        @JsonProperty("main_address_no")
        private String mainAddressNo;

        @JsonProperty("sub_address_no")
        private String subAddressNo;

        @JsonProperty("mountain_yn")
        private String mountainYn;

        @JsonProperty("b_code")
        private String bCode;

        @JsonProperty("h_code")
        private String hCode;

        @JsonProperty("x")
        private String x;

        @JsonProperty("y")
        private String y;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RoadAddress {

        @JsonProperty("address_name")
        private String addressName;

        @JsonProperty("region_1depth_name")
        private String region1depthName;

        @JsonProperty("region_2depth_name")
        private String region2depthName;

        @JsonProperty("region_3depth_name")
        private String region3depthName;

        @JsonProperty("road_name")
        private String roadName;

        @JsonProperty("underground_yn")
        private String undergroundYn;

        @JsonProperty("main_building_no")
        private String mainBuildingNo;

        @JsonProperty("sub_building_no")
        private String subBuildingNo;

        @JsonProperty("building_name")
        private String buildingName;

        @JsonProperty("zone_no")
        private String zoneNo;

        @JsonProperty("x")
        private String x;

        @JsonProperty("y")
        private String y;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meta {

        @JsonProperty("total_count")
        private int totalCount;

        @JsonProperty("pageable_count")
        private int pageableCount;

        @JsonProperty("is_end")
        private boolean isEnd;
    }
}