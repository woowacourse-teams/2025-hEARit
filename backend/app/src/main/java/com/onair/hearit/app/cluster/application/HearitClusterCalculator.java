package com.onair.hearit.app.cluster.application;

import com.onair.hearit.app.cluster.dto.NormalizedHearitClusterFeature;
import com.onair.hearit.core.domain.HearitCluster;
import com.onair.hearit.core.infrastructure.jdbc.HearitClusterCommandRepository;
import com.onair.hearit.core.infrastructure.jpa.ClusteredHearitRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smile.clustering.CentroidClustering;
import smile.clustering.Clustering;
import smile.clustering.KMeans;

@Service
@RequiredArgsConstructor
public class HearitClusterCalculator {

    private static final int MAX_KMEANS_ITERATION = 1000;
    private static final double TOLERANCE = 1e-15;

    private final ClusteredHearitRepository clusteredHearitRepository;
    private final HearitClusterCommandRepository hearitClusterCommandRepository;
    private final HearitClusterFeatureNormalizer hearitClusterFeatureNormalizer;

    @Transactional
    public void calculateClusters(int k) {
        List<HearitCluster> rowFeatures = clusteredHearitRepository.findAll();

        if (rowFeatures.isEmpty()) {
            return;
        }

        List<NormalizedHearitClusterFeature> normalizedFeatures = hearitClusterFeatureNormalizer
                .normalizeFromEntities(rowFeatures);
        double[][] data = normalizedFeatures.stream()
                .map(NormalizedHearitClusterFeature::vector)
                .toArray(double[][]::new);

        int actualK = Math.min(rowFeatures.size(), k); // 데이터 개수(n)가 설정된 k보다 적으면 n을 k로 사용
        Clustering.Options options = new Clustering.Options(actualK, MAX_KMEANS_ITERATION, TOLERANCE, null);
        CentroidClustering<double[], double[]> clustering = KMeans.fit(data, options);
        int[] labels = clustering.group();

        Map<Long, Integer> clusterResults = IntStream.range(0, normalizedFeatures.size()).boxed()
                .collect(Collectors.toMap(
                        i -> normalizedFeatures.get(i).hearitId(),
                        i -> labels[i]
                ));
        hearitClusterCommandRepository.updateClusterIds(clusterResults);
    }
}
