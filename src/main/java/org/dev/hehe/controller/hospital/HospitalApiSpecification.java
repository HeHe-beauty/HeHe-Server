package org.dev.hehe.controller.hospital;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.dev.hehe.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.dev.hehe.dto.hospital.HospitalClusterRequest;
import org.dev.hehe.dto.hospital.HospitalDetailResponse;
import org.dev.hehe.dto.hospital.HospitalListResponse;
import org.dev.hehe.dto.hospital.HospitalMapRequest;
import org.dev.hehe.dto.hospital.HospitalMapResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Hospital API Swagger 명세 인터페이스
 * 실제 구현은 HospitalController
 */
@Tag(name = "Hospital", description = "병원 지도 API")
public interface HospitalApiSpecification {

    @Operation(
            summary = "지도 클러스터 조회",
            description = """
                    현재 지도 뷰포트 내 병원 클러스터 목록을 반환합니다.

                    - 줌 레벨에 따라 좌표 반올림 자릿수(precision)가 결정됩니다.
                    - 응답의 precision 값은 클러스터 클릭 시 목록 API에 그대로 전달해야 합니다.
                    - equipId 를 전달하면 해당 장비를 보유한 병원만 필터링됩니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "precision": 2,
                                        "items": [
                                          { "count": 23, "lat": 37.52, "lng": 127.05 },
                                          { "count": 8,  "lat": 37.48, "lng": 127.03 }
                                        ]
                                      }
                                    }
                                    """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "필수 파라미터 누락",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "errorCode": "C002", "message": "잘못된 요청 값입니다." }
                                    """))
            )
    })
    ApiResponse<HospitalMapResponse> getMapClusters(@ParameterObject @Valid HospitalMapRequest request);

    @Operation(
            summary = "클러스터 내 병원 목록 조회",
            description = """
                    클러스터 클릭 시 하단 시트에 표시할 병원 목록을 반환합니다.

                    - lat, lng, precision 은 지도 클러스터 API 응답값을 그대로 전달합니다.
                    - equipId 를 전달하면 해당 장비를 보유한 병원만 필터링됩니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": [
                                        {
                                          "hospitalId": 101,
                                          "name": "강남 제모 클리닉",
                                          "address": "서울 강남구 역삼동 123-4",
                                          "tags": ["여성원장", "주차가능"]
                                        }
                                      ]
                                    }
                                    """))
            )
    })
    ApiResponse<List<HospitalListResponse>> getHospitalsByCluster(@ParameterObject @Valid HospitalClusterRequest request);

    @Operation(
            summary = "병원 상세 조회",
            description = "병원 ID로 상세 정보(태그, 보유 장비 포함)를 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "hospitalId": 101,
                                        "name": "강남 제모 클리닉",
                                        "address": "서울 강남구 역삼동 123-4",
                                        "lat": 37.512,
                                        "lng": 127.059,
                                        "contactNumber": "02-1234-5678",
                                        "contactUrl": "https://...",
                                        "tags": ["여성원장", "주차가능"],
                                        "equipments": [
                                          { "modelName": "젠틀맥스프로", "totalCount": 2 }
                                        ]
                                      }
                                    }
                                    """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "병원 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "errorCode": "H001", "message": "병원을 찾을 수 없습니다." }
                                    """))
            )
    })
    ApiResponse<HospitalDetailResponse> getHospitalDetail(
            @Parameter(description = "조회할 병원 ID", required = true) @PathVariable Long hospitalId
    );
}