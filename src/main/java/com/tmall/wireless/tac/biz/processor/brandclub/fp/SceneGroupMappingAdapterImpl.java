package com.tmall.wireless.tac.biz.processor.brandclub.fp;


/**
 * @author xingguan.wzt
 * @date 2021/08/18
 */

public class SceneGroupMappingAdapterImpl {
//    private final static String SCENE_CHANNEL = "sceneLdb";
//    private static final String GROUP_MAPPING_PREFIX = "tcls_ugc_scenegroup_mapping_v1";
//
//    @Autowired
//    private ChannelGateWay dataChannel;
//
//    @Override
//    public void saveGroupAndBrandMapping(Collection<SceneGroup> groups) {
//        ChannelWriteDO writeData = new ChannelWriteDO();
//        writeData.setAction("update");
//        writeData.setChannelName(SCENE_CHANNEL);
//
//        Map<Long, List<SceneGroup>> mappings = Maps.newHashMap();
//        for (SceneGroup group : groups) {
//            Map<String, Object> attr = group.getAttributes();
//            Long brandId = MapUtils.getLong(attr, "brandId");
//            if (brandId == null) {
//                continue;
//            }
//            List<SceneGroup> mgroups = mappings.computeIfAbsent(brandId, k -> new ArrayList<>());
//            mgroups.add(group);
//        }
//
//        List<KvPair> kvPairs = new ArrayList<>();
//        mappings.forEach((brandId, mgroups) -> {
//            KvPair kv = new KvPair();
//            kv.setPk(genGroupAndBrandMappingKey(brandId));
//            Map<String, List<Long>> data = new HashMap<>(1 << 2);
//            mgroups.forEach(mgroup -> {
//                Long groupId = IdUtils.toLong(mgroup.getId());
//                Map<String, Object> attr = mgroup.getAttributes();
//                GroupExtBrandProp prop = JSON.parseObject(JSON.toJSONString(attr), GroupExtBrandProp.class);
//                switch (prop.getType()) {
//                    case BOARD:
//                        data.computeIfAbsent("boardSceneGroupIds", k -> new ArrayList<>()).add(groupId);
//                        break;
//                    case GENERAL:
//                    default:
//                        data.computeIfAbsent("generalSceneGroupIds", k -> new ArrayList<>()).add(groupId);
//                }
//            });
//            kv.setData(JSONObject.toJSONString(data));
//            kvPairs.add(kv);
//        });
//        writeData.setKvPairs(kvPairs);
//        SingleResponse<ChannelWriteResp> rs = dataChannel.update(writeData);
//        isTrue(rs.isSuccess(), CAPTAIN_PUT_FAILED, rs.getErrCode(), rs.getErrMessage());
//
//    }
//
//    @Override
//    public Map<String, Map<String, Object>> getGroupAndBrandMapping(Collection<Long> brandIds) {
//        Collection<String> propKeys = Lists.newArrayList("boardSceneGroupIds", "generalSceneGroupIds");
//        List<ChannelDataDO> propQuery = propKeys.stream().map(propKey -> {
//            ChannelDataDO query = new ChannelDataDO();
//            query.setChannelName(SCENE_CHANNEL);
//            query.setDataKey(propKey);
//            query.setChannelField(propKey);
//            return query;
//        }).collect(Collectors.toList());
//        List<String> keys = brandIds.stream().map(this::genGroupAndBrandMappingKey).collect(Collectors.toList());
//        SingleResponse<Map<String, Map<String, Object>>> rs = dataChannel.queryChannelData(propQuery, keys, null);
//        isTrue(rs.isSuccess(), CAPTAIN_GET_FAILED, rs.getErrCode(), rs.getErrMessage());
//        return rs.getData();
//    }
//
//    private String genGroupAndBrandMappingKey(Long brandId) {
//        return genGroupMappingKey("btao", brandId);
//    }
//
//    private String genGroupMappingKey(Object... keys) {
//        noNullElements(keys);
//        return GROUP_MAPPING_PREFIX + "_" + StringUtils.join(keys, "_");
//    }
}
